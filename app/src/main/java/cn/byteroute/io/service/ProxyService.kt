package cn.byteroute.io.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.net.VpnService
import android.os.*
import android.text.format.Formatter
import android.util.Log
import cn.byteroute.io.ByteNative
import cn.byteroute.io.IProxyService
import cn.byteroute.io.IProxyServiceCallback
import cn.byteroute.io.R
import cn.byteroute.io.annotation.ProxyState
import cn.byteroute.io.common.*
import cn.byteroute.io.data.Config
import cn.byteroute.io.ext.KeyValue
import cn.byteroute.io.helper.NotificationHelper.createNotification
import cn.byteroute.io.helper.NotificationHelper.createNotificationChannel
import cn.byteroute.io.helper.NotificationHelper.createNotify
import cn.byteroute.io.helper.NotificationHelper.isOpen
import cn.byteroute.io.helper.NotificationHelper.updateNotificationChannel
import cn.byteroute.io.utils.ProfileUtils
import kotlinx.coroutines.*
import tun2socks.Options
import tun2socks.Tun2socks
import kotlin.coroutines.CoroutineContext

class ProxyService : VpnService(), CoroutineScope {
    @ProxyState
    private var status = ProxyState.STATE_IDLE
    private var rxBytes: Long = 0
    private var preRxBytes: Long = Long.MIN_VALUE
    private var txBytes: Long = 0
    private var preTxBytes: Long = Long.MIN_VALUE
    private var pfd: ParcelFileDescriptor? = null
    private val mCallbackList = RemoteCallbackList<IProxyServiceCallback>()
    private val stopReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Constants.ACTION_STOP -> stopProxyService()
                    Constants.SCREEN_OFF -> if (keyValue[Key.SCREEN_OFF, false]) stopProxyService()
                }
            }
        }
    }

    private val keyValue by lazy {
        KeyValue(this)
    }
    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private val TAG = "ProxyService"
    private fun setState(state: Int) {
        Log.d(TAG, "setState: ")
        this.status = state
        broadcast { it.onStateChanged(state, "state changed") }
    }

    override fun onCreate() {
        super.onCreate()
        job = Job()
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_STOP)
        filter.addAction("android.intent.action.SCREEN_OFF");//增加熄屏操作
        registerReceiver(stopReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        setState(ProxyState.STATE_STOPPED)
        mCallbackList.kill()
        unregisterReceiver(stopReceiver)
        pfd = null
    }

    override fun onRevoke() {
        stopProxyService()
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (Constants.ACTION_BIND == intent.action) {
            object : IProxyService.Stub(), CoroutineScope, AutoCloseable {
                @Throws(RemoteException::class)
                override fun getState(): Int {
                    return status
                }

                @Throws(RemoteException::class)
                override fun registerCallback(callback: IProxyServiceCallback) {
                    mCallbackList.register(callback)
                }

                @Throws(RemoteException::class)
                override fun unregisterCallback(callback: IProxyServiceCallback) {
                    mCallbackList.unregister(callback)
                }

                override val coroutineContext: CoroutineContext
                    get() = Dispatchers.IO + Job()

                override fun close() {
                    mCallbackList.kill()
                }
            }
        } else super.onBind(intent)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when {
            intent != null && SERVICE_INTERFACE == intent.action && (status == ProxyState.STATE_STOPPED || status == ProxyState.STATE_IDLE) -> {
                createNotificationChannel(this, getString(R.string.notification_channel_id))
                startForeground(
                    Constants.NOTIFICATION_ID, createNotification(
                        this,
                        getString(R.string.notification_channel_id),
                        R.drawable.ic_notification,
                        getString(R.string.app_name),
                        ""
                    )
                )
                setState(ProxyState.STATE_CONNECTING)
                var bypass = keyValue[Key.BYPASS_ROUTE, false]//绕过ip
                var config = keyValue[Key.CONFIG, Config::class.java]
                Builder().apply {
                    try {
                        var list = keyValue.getList(
                            Key.PACKAGE_LIST,
                            String::class.java
                        )
                        addDisallowedApplication(packageName) //全局模式
                        if (list != null && list.size > 0) {
                            list.forEach {
                                addDisallowedApplication(it)
                            }
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        setMetered(false)
                    }
                    setSession(getString(R.string.app_name))
                    setMtu(Constants.VPN_MTU)
                    addAddress(Constants.PRIVATE_VLAN4_CLIENT, 30)
                    var ipv6: Boolean = keyValue[Key.ENABLE_IPV6, false].apply {
                        addAddress(Constants.PRIVATE_VLAN6_CLIENT, 126)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val connectivityManager =
                            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork = connectivityManager.activeNetwork
                        if (activeNetwork != null) {
                            setUnderlyingNetworks(arrayOf(activeNetwork))
                        }
                    }
                    if (bypass) {
                        for (route in resources.getStringArray(R.array.bypass_private_route)) {
                            val parts = route.split("/")
                            addRoute(parts[0], parts[1].toInt())
                        }
                        addRoute("198.18.0.0", 16)
                        if (ipv6) {
                            addRoute("2000::", 3)
                        }
                    } else {
                        addRoute("0.0.0.0", 0)
                        if (ipv6) {
                            addRoute("::", 0)
                        }
                    }
                    addDnsServer("216.146.35.35")
                    addDnsServer("216.146.36.36")
                    addDnsServer("208.67.222.222")
                    addDnsServer("208.67.220.220")
                    addDnsServer("8.8.8.8")
                    addDnsServer("8.8.4.4")
                    addDnsServer("1.1.1.1")
                    addDnsServer("1.0.0.1")
                    if (ipv6) {
                        addDnsServer("2001:4860:4860::8888")
                        addDnsServer("2001:4860:4860::8844")
                    }
                    pfd = establish()
                    if (pfd == null) {
                        stopProxyService()
                    }
                    val fd = pfd?.detachFd()

                    if (config == null) {
                        stopProxyService()
                    }
                    var config = ProfileUtils.createProfile(
                        1080,
                        config.remoteAddr,
                        config.remotePort,
                        config.password
                    )
                    ByteNative.startClient(config)
                    Options().apply {
                        tunFd = fd?.toLong()!!
                        socks5Server = "${Constants.LOCAL_LOOPBACK}:${Constants.PORT}"
                        //socks5Server = "10.205.29.206:1080"
                        enableIPv6 = false
                        mtu = Constants.VPN_MTU.toLong()
                        allowLan = true
                        Tun2socks.start(this)
                    }
                }
                setState(ProxyState.STATE_CONNECTED)
                launch {
                    var notify = isOpen(this@ProxyService)
                    while (notify) {
                        rxBytes = TrafficStats.getTotalRxBytes() //接收
                        txBytes = TrafficStats.getTotalTxBytes() //发送
                        var tx = Math.abs((txBytes - preTxBytes) / 2)
                        if (preTxBytes == Long.MIN_VALUE) {
                            tx = 0
                        }
                        var rx = Math.abs((rxBytes - preRxBytes) / 2)
                        if (preRxBytes == Long.MIN_VALUE) {
                            rx = 0
                        }
                        updateNotificationChannel(
                            this@ProxyService, createNotify(
                                this@ProxyService,
                                getString(R.string.notification_channel_id),
                                R.drawable.ic_notification,
                                when {
                                    config.remoteRemark.isNullOrEmpty() -> config.remoteAddr
                                    else -> config.remoteRemark
                                },
                                getString(
                                    R.string.traffic,
                                    Formatter.formatFileSize(
                                        this@ProxyService,
                                        tx
                                    ),
                                    Formatter.formatFileSize(
                                        this@ProxyService,
                                        rx
                                    )
                                )
                            )
                        )
                        preRxBytes = rxBytes
                        preTxBytes = txBytes
                        delay(2000)
                    }
                }
            }
        }
        return START_STICKY
    }


    private fun broadcast(work: (IProxyServiceCallback) -> Unit) {
        val count = mCallbackList.beginBroadcast()
        try {
            repeat(count) {
                try {
                    work(mCallbackList.getBroadcastItem(it))
                } catch (e: Exception) {
                }
            }
        } finally {
            mCallbackList.finishBroadcast()
        }
    }

    fun stopProxyService() {
        setState(ProxyState.STATE_STOPPING)
        ByteNative.stopClient()
        Tun2socks.stop()
        stopSelf()
        setState(ProxyState.STATE_STOPPED)
        stopForeground(true)
        Process.killProcess(Process.myPid())
    }
}
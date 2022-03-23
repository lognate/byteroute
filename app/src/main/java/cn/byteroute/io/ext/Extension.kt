package cn.byteroute.io.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.Checkable
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.keyvalue.KeyValue
import androidx.keyvalue.KeyValueInitializer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.startup.AppInitializer
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.*
import java.util.*
import kotlin.collections.HashSet

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()


val Float.sp: Float                 // [xxhdpi](360 -> 1080)
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )


val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()


val String.domain: String
    get() {
        try {
            return InetAddress.getByName(this).hostAddress
        } catch (e: Exception) {
            return ""
        }
    }

val String.ping: Int
    get() {
        var process = ProcessBuilder(arrayListOf("ping", "-c", "1", "-w", "5", this)).start()
        var line: String? = ""
        var buffere = BufferedReader(InputStreamReader(process.inputStream))
        while (buffere.readLine().also { line = it } != null) {
            if (line != null) {
                var index = line!!.indexOf("time=")
                if (index!! > 0) {
                    var split = line!!.substring(index).split("=")
                    if (split.size > 1) {
                        return split[1].split(" ")[0].toFloat().toInt()
                    }
                }
            }
        }
        buffere.close()
        return -1
    }

val String.proxyPing: Int get() = proxyPing("127.0.0.1", 1080, this)

val Context.wifi: WifiManager get() = this.getSystemService(Context.WIFI_SERVICE) as WifiManager

val Context.openWifi: Boolean
    get() {
        return if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true)
        } else {
            true
        }
    }

val Activity.startScan: Int
    get() = ScanUtil.startScan(this, Constants.SCAN_CODE, HmsScanAnalyzerOptions.Creator().create())

fun isMain(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}

inline fun <T : View> T.singleClick(time: Long = 800, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

//兼容点击事件设置为this的情况
fun <T : View> T.singleClick(onClickListener: View.OnClickListener, time: Long = 800) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            onClickListener.onClick(this)
        }
    }
}

var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0

fun <T> List<T>.duplicate(): List<T> {
    val set = HashSet<T>(this)
    val result = ArrayList<T>()
    result.addAll(set)
    return result
}

fun KeyValue(context: Context): KeyValue {
    return AppInitializer.getInstance(context).initializeComponent(
        KeyValueInitializer::class.java
    )
}

val View.visible: Unit
    get() {
        this.visibility = View.VISIBLE
    }
val View.gone: Unit
    get() {
        this.visibility = View.GONE
    }

//本机IP地址
fun localIP(): List<String> {
    var list = arrayListOf<String>()
    try {
        val networkInterfaces: Enumeration<NetworkInterface> =
            NetworkInterface.getNetworkInterfaces()
        var temp: InetAddress
        while (networkInterfaces.hasMoreElements()) {
            val tempInterface: NetworkInterface =
                networkInterfaces.nextElement() as NetworkInterface
            if (!tempInterface.isLoopback) {
                val inetAddresses: Enumeration<InetAddress> = tempInterface.getInetAddresses()
                while (inetAddresses.hasMoreElements()) {
                    temp = inetAddresses.nextElement()
                    if (temp is Inet4Address) {
                        list.add(temp.getHostAddress())
                    }
                }
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    if (list.isEmpty()) {
        list.add("0.0.0.0")
    }
    return list
}

//端口检测
fun telnet(host: String, port: Int): Boolean {
    try {
        return Socket(host, port).isConnected
    } catch (e: Exception) {
        return false
    }
}

fun proxyPing(proxyHost: String, proxyPort: Int, url: String): Int {
    try {
        val startTime = System.currentTimeMillis()
        val proxyAddress = InetSocketAddress(proxyHost, proxyPort)
        val proxy = Proxy(Proxy.Type.SOCKS, proxyAddress)
        val connection = URL(url).openConnection(proxy)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.connect()
        return (System.currentTimeMillis() - startTime).toInt()
    } catch (e: Exception) {
        return -1
    }
}


fun tcpPing(host: String, port: Int): Int {
    try {
        val startTime = System.currentTimeMillis()
        Socket(host, port)
        return (System.currentTimeMillis() - startTime).toInt()
    } catch (e: Exception) {
        return -1
    }
}

suspend fun connectWifi(context: Context, ssid: String?, password: String?, type: Int): Boolean {
    if (!context.openWifi) {
        return false
    }
    while (context.wifi.wifiState == WifiManager.WIFI_STATE_ENABLING) {
        delay(100)
    }
    val netID: Int = createWifiInfo(context.wifi, ssid, password, type)
    val bRet: Boolean = context.wifi.enableNetwork(netID, true)
    context.wifi.saveConfiguration()
    return bRet
}

fun createWifiInfo(wifiManager: WifiManager, ssid: String?, password: String?, type: Int): Int {
    val config = WifiConfiguration()
    config.allowedAuthAlgorithms.clear()
    config.allowedGroupCiphers.clear()
    config.allowedKeyManagement.clear()
    config.allowedPairwiseCiphers.clear()
    config.allowedProtocols.clear()
    config.SSID = "\"" + ssid + "\""
    val tempConfig: WifiConfiguration? = isExsits(wifiManager, ssid)
    if (tempConfig != null) {
        if (!wifiManager.removeNetwork(tempConfig.networkId)) {
            return tempConfig.networkId
        }
    }
    if (type == HmsScan.WiFiConnectionInfo.NO_PASSWORD_MODE_TYPE) //WIFICIPHER_NOPASS
    {
        config.wepKeys[0] = ""
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        config.wepTxKeyIndex = 0
    }
    if (type == HmsScan.WiFiConnectionInfo.WEP_MODE_TYPE) //WIFICIPHER_WEP
    {
        config.hiddenSSID = true
        config.wepKeys[0] = "\"" + password + "\""
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        config.wepTxKeyIndex = 0
    }
    if (type == HmsScan.WiFiConnectionInfo.WPA_MODE_TYPE) //WIFICIPHER_WPA
    {
        config.preSharedKey = "\"" + password + "\""
        config.hiddenSSID = true
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        config.status = WifiConfiguration.Status.ENABLED
    }
    return wifiManager.addNetwork(config)
}

@SuppressLint("MissingPermission")
fun isExsits(wifiManager: WifiManager, SSID: String?): WifiConfiguration? {
    val existingConfigs: List<WifiConfiguration> = wifiManager.getConfiguredNetworks()
    for (existingConfig in existingConfigs) {
        if (existingConfig.SSID == "\"" + SSID + "\"") {
            return existingConfig
        }
    }
    return null
}

fun <T : BaseViewModel> Any.clazz(): Class<T>? {
    val genericSuperclass: Type? = javaClass.getGenericSuperclass()
    if (genericSuperclass is ParameterizedType) {
        val actualTypeArguments = genericSuperclass.actualTypeArguments
        if (actualTypeArguments.size > 0) {
            return actualTypeArguments[0] as Class<T>
        }
    }
    return null
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, block: (T) -> Unit) {
    liveData.observe(this, { block.invoke(it) })
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


fun <T> Context.preferencesValue(pKey: Preferences.Key<T>): Flow<T?> = dataStore.data.map {
    it[pKey]
}

fun <T> Context.preferencesKey(pKey: Preferences.Key<T>, data: T) =
    GlobalScope.launch(Dispatchers.IO) {
        dataStore.edit {
            it[pKey] = data
        }
    }
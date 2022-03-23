package cn.byteroute.io

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.RemoteException
import cn.byteroute.io.common.Constants
import cn.byteroute.io.service.ProxyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ProxyConnection(listenForDeath: Boolean) : DeathRecipient,
    ServiceConnection {
    private var mService: IProxyService? = null
    private var mCallback: Callback? = null
    private var mRegistered = false
    private var mConnected = false
    private var mBinder: IBinder? = null
    private val listenForDeath: Boolean
    private val mServiceCallback: IProxyServiceCallback = object : IProxyServiceCallback.Stub() {
        @Throws(RemoteException::class)
        override fun onStateChanged(state: Int, msg: String) {
            GlobalScope.launch(Dispatchers.Main.immediate) {
                mCallback?.onStateChanged(state, msg)
            }
        }
    }

    fun connectService(context: Context, callback: Callback?) {
        if (mConnected) {
            return
        }
        mConnected = true
        check(mCallback == null) { "Required to call disconnect(Context) first." }
        mCallback = callback
        val intent = Intent(context, ProxyService::class.java)
        intent.action = Constants.ACTION_BIND
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun disconService(context: Context) {
        unregisterServiceCallback()
        if (mConnected) {
            try {
                context.unbindService(this)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            mConnected = false
            if (listenForDeath && mBinder != null) {
                mBinder?.unlinkToDeath(this, 0)
            }
            mBinder = null
            mService = null
            mCallback = null
        }
    }

    private fun unregisterServiceCallback() {
        if (mService != null && mRegistered) {
            try {
                mService?.unregisterCallback(mServiceCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            mRegistered = false
        }
    }

    fun getService(): IProxyService? {
        return mService
    }

    override fun binderDied() {
        mService = null
        mRegistered = false
        mCallback?.also { GlobalScope.launch(Dispatchers.Main.immediate) { it.onBinderDied() } }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        mBinder = service
        val mService = IProxyService.Stub.asInterface(service)
        this.mService = mService
        try {
            if (listenForDeath) {
                service.linkToDeath(this, 0)
            }
            check(!mRegistered) { "ServiceCallback already registered!" }
            mService.registerCallback(mServiceCallback)
            mRegistered = true
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        mCallback?.onServiceConnected(mService)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        unregisterServiceCallback()
        mCallback?.onServiceDisconnected()
        mService = null
        mBinder = null
    }

    interface Callback {
        fun onServiceConnected(service: IProxyService?) {}
        fun onServiceDisconnected() {}
        fun onStateChanged(state: Int, msg: String?) {}
        fun onBinderDied() {}
    }

    init {
        this.listenForDeath = listenForDeath
    }
}

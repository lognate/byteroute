package cn.byteroute.io.service

import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import cn.byteroute.io.IProxyService
import cn.byteroute.io.ProxyConnection
import cn.byteroute.io.annotation.ProxyState
import cn.byteroute.io.common.Key
import cn.byteroute.io.data.Config
import cn.byteroute.io.ext.KeyValue
import cn.byteroute.io.helper.ContextHelper.startProxyService
import cn.byteroute.io.helper.ContextHelper.stopProxyService
import cn.byteroute.io.ui.MainActivity

@RequiresApi(api = Build.VERSION_CODES.N)
class ByterouteTileService : TileService(), ProxyConnection.Callback {
    private val connection = ProxyConnection(false)
    private var mTapPending = false
    override fun onStartListening() {
        super.onStartListening()
        connection.connectService(this, this)
    }

    override fun onStopListening() {
        super.onStopListening()
        connection.disconService(this)
    }

    override fun onClick() {
        super.onClick()
        val service = connection.getService()
        if (service == null) {
            mTapPending = true
            updateTile(ProxyState.STATE_CONNECTING)
        } else {
            try {
                updateTile(service.state)
                when (service.state) {
                    ProxyState.STATE_CONNECTED -> stopProxyService(this)
                    ProxyState.STATE_CONNECTING, ProxyState.STATE_STOPPING -> {
                    }
                    ProxyState.STATE_IDLE, ProxyState.STATE_STOPPED -> {
                        if (KeyValue(this)[Key.CONFIG, Config::class.java] == null) {
                            Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(this)
                            }
                        } else {
                            startProxyService(this)
                        }
                    }
                    else -> {
                    }
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateTile(@ProxyState state: Int) {
        val tile = qsTile ?: return
        when (state) {
            ProxyState.STATE_IDLE, ProxyState.STATE_STOPPED -> tile.state = Tile.STATE_INACTIVE
            ProxyState.STATE_CONNECTING, ProxyState.STATE_CONNECTED -> tile.state =
                Tile.STATE_ACTIVE
            ProxyState.STATE_STOPPING -> tile.state = Tile.STATE_UNAVAILABLE
            else -> {
            }
        }
        tile.updateTile()
    }

    override fun onServiceConnected(service: IProxyService?) {
        try {
            val state = service!!.state
            updateTile(state)
            if (mTapPending) {
                mTapPending = false
                onClick()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onServiceDisconnected() {
        updateTile(ProxyState.STATE_STOPPED)
    }

    override fun onStateChanged(state: Int, msg: String?) {
        updateTile(state)
    }

    override fun onBinderDied() {
    }
}
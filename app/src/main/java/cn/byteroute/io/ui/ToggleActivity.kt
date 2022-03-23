package cn.byteroute.io.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import cn.byteroute.io.IProxyService
import cn.byteroute.io.ProxyConnection
import cn.byteroute.io.R
import cn.byteroute.io.annotation.ProxyState.STATE_CONNECTED
import cn.byteroute.io.helper.ContextHelper

class ToggleActivity : Activity(), ProxyConnection.Callback {
    var connection = ProxyConnection(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            setResult(
                RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(
                    this,
                    ShortcutInfoCompat.Builder(this, "toggle")
                        .setIntent(
                            Intent(
                                this,
                                ToggleActivity::class.java
                            ).setAction(Intent.ACTION_MAIN)
                        )
                        .setIcon(IconCompat.createWithResource(this, R.drawable.ic_toggle))
                        .setShortLabel(getString(R.string.quick_toggle))
                        .build()
                )
            )
            finish()
        } else {
            connection.connectService(this, this)
            if (Build.VERSION.SDK_INT >= 25) getSystemService<ShortcutManager>()?.reportShortcutUsed(
                "toggle"
            )
        }
    }

    override fun onServiceConnected(service: IProxyService?) {
        when {
            service?.state == STATE_CONNECTED -> ContextHelper.stopProxyService(this)
            else -> ContextHelper.startProxyService(this)
        }
        finish()
    }

    override fun onDestroy() {
        connection.disconService(this)
        super.onDestroy()
    }
}
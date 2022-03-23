package cn.byteroute.io.helper

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import cn.byteroute.io.common.Constants
import cn.byteroute.io.common.Key
import cn.byteroute.io.service.ProxyService
import cn.byteroute.io.ui.WebViewActivity

object ContextHelper {

    fun startProxyService(context: Context) {
        var prepare: Intent? = VpnService.prepare(context)
        if (prepare == null) {
            var intent = Intent(context, ProxyService::class.java)
            intent.action = VpnService.SERVICE_INTERFACE
            //context.startService(intent)
            ContextCompat.startForegroundService(context!!, intent)
        }
    }


    fun stopProxyService(context: Context) {
        var intent = Intent(Constants.ACTION_STOP)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    fun startWeb(context: Context, url: String?, title: String?, type: Int) {
        var intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(Key.WEB_URL, url)
        intent.putExtra(Key.WEB_TITLE, title)
        intent.putExtra(Key.WEB_TYPE, type)
        context.startActivity(intent)
    }
}
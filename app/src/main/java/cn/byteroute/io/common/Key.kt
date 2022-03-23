package cn.byteroute.io.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object Key {
    const val ENABLE_IPV6 = "enable_ipv6"
    const val PACKAGE_LIST = "package_list"
    const val WEB_TITLE = "web_tile"
    const val WEB_URL = "web_url"
    const val WEB_TYPE = "web_type"
    const val BYPASS_ROUTE = "bypass_route"
    const val VPN_AP = "vpn_ap"
    const val SCREEN_OFF = "screen_off"
    const val AP_SHARE = "ap_share"
    const val CONFIG = "config"
    const val PING_MODE = "ping_mode"
    class Test{

        val ENABLE_IPV6 = stringPreferencesKey("enable_ipv6")
        val PACKAGE_LIST = stringSetPreferencesKey("package_list")
        val WEB_TITLE = stringPreferencesKey("web_tile")
        val WEB_URL = stringPreferencesKey("web_url")
        val WEB_TYPE = stringPreferencesKey("web_type")
        val BYPASS_ROUTE = stringPreferencesKey("bypass_route")
        val VPN_AP = stringPreferencesKey("vpn_ap")
        val SCREEN_OFF = stringPreferencesKey("screen_off")
        val AP_SHARE = stringPreferencesKey("ap_share")
        val CONFIG = stringPreferencesKey("config")
        val PING_MODE = stringPreferencesKey("ping_mode")
    }
}



/*
object Key {
    val ENABLE_IPV6 = stringPreferencesKey("enable_ipv6")
    val PACKAGE_LIST = stringSetPreferencesKey("package_list")
    val WEB_TITLE = stringPreferencesKey("web_tile")
    val WEB_URL = stringPreferencesKey("web_url")
    val WEB_TYPE = stringPreferencesKey("web_type")
    val BYPASS_ROUTE = stringPreferencesKey("bypass_route")
    val VPN_AP = stringPreferencesKey("vpn_ap")
    val SCREEN_OFF = stringPreferencesKey("screen_off")
    val AP_SHARE = stringPreferencesKey("ap_share")
    val CONFIG = stringPreferencesKey("config")
    val PING_MODE = stringPreferencesKey("ping_mode")
}*/

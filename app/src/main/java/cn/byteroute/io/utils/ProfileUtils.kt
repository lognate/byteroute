package cn.byteroute.io.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cn.byteroute.io.common.Constants
import cn.byteroute.io.data.Config
import java.io.*

object ProfileUtils {
    fun createProfile(
        localPort: Int,
        remoteAddr: String,
        remotePort: Int,
        password: String,
    ): String {
        var hashMap = hashMapOf<String, Any>().apply {
            put("run_type", "client")
            put("local_addr", Constants.LOCAL_LOOPBACK)
            put("local_port", localPort)
            put("remote_addr", remoteAddr)
            put("remote_port", remotePort)
            put("password", arrayOf(password))
            put("ssl", hashMapOf<String, Any>().apply {
                put("verify", false)
                put(
                    "cipher",
                    "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES128-SHA:ECDHE-RSA-AES256-SHA:DHE-RSA-AES128-SHA:DHE-RSA-AES256-SHA:AES128-SHA:AES256-SHA:DES-CBC3-SHA"
                )
                put(
                    "cipher_tls13",
                    "TLS_AES_128_GCM_SHA256:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_256_GCM_SHA384"
                )
                put("sni", remoteAddr)
            })
           /* put("router", hashMapOf<String, Any>().apply {
                put("enabled", true)
                //put("bypass", arrayOf("geoip:cn", "geoip:private", "geosite:cn", "geosite:geolocation-cn"))
                put("bypass", arrayOf("geoip:cn",
                    "geoip:private",
                    "geosite:cn",
                    "geosite:private"))
                put("block", arrayOf("keyword:youku","geosite:category-ads"))
                // put("block", arrayOf("geosite:category-ads","domain:baidu.com"))
                put("proxy", arrayOf("geosite:geolocation-!cn"))
                // put("proxy", arrayOf("geosite:geolocation-!cn"))
                put("default_policy", "bypass")
                put("geoip", geoip)
                put("geosite", geosite)
            })*/
        }
        return Gson().toJson(hashMap)
    }

    fun updateProfile(context: Context, profile: String?) {
        try {
            val config = File(context.filesDir, Constants.FILE_CONFIG)
            val outputStream = FileOutputStream(config)
            outputStream.write(profile?.toByteArray())
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun updateProfile(
        file: File,
        remoteRemark: String,
        remoteAddr: String,
        remotePort: Int,
        password: String,
        localPort: Int = 1080,
        path: String
    ) {
        createProfile(
            localPort,
            remoteAddr,
            remotePort,
            password)?.apply { updateProfile(file, this) }
    }

    fun updateProfile(file: File, profile: String?) {
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(profile?.toByteArray())
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun readProfile(context: Context): String? {
        val config = File(context.filesDir, "logFile.txt")
        if (config.exists()) {
            try {
                val inputStream = FileInputStream(File(context.filesDir, Constants.FILE_CONFIG))
                val content = ByteArray(config.length().toInt())
                inputStream.read(content)
                inputStream.close()
                return String(content)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun readConfigArray(context: Context): List<Config> {
        var open = context.assets.open("data.json")
        var byteArray: ByteArray = ByteArray(open.available())
        open.read(byteArray)
        return Gson().fromJson(
            String(byteArray),
            object : TypeToken<List<Config>>() {}.type
        )
    }

    fun <T> readAssets(context: Context, fileName: String): List<T>? {
        try {
            val inputStream = context.assets.open("data.json")
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            return Gson().fromJson(String(bytes), object : TypeToken<List<T>?>() {}.type)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun <T> readAssets(context: Context, fileName: String, tClass: Class<T>?): T? {
        try {
            val inputStream = context.assets.open(fileName)
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            return Gson().fromJson(String(bytes), tClass)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
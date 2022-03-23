package cn.byteroute.io

import android.content.Context
import androidx.startup.Initializer
import cn.byteroute.io.utils.FileUtils

class ByteInitializer : Initializer<Context?> {
    override fun create(context: Context): Context {
        FileUtils.copyAssets(context)
        return context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> {
        return emptyList()
    }
}
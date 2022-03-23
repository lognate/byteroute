package cn.byteroute.io.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.*

object FileUtils {
    fun copyAssets(context: Context) {
       copyAssets(context, "files", context.filesDir)
    }

    fun copyAssets(context: Context, dir: String, file: File) {
        try {
            val fileNames = context.assets.list(dir)
            for (fileName in fileNames!!) {
                val file = File(file, fileName)
                if (!file.exists()) {
                    val inputStream = context.assets.open("$dir/$fileName")
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var byteCount: Int
                    while (inputStream.read(buffer).also { byteCount = it } != -1) {
                        outputStream.write(buffer, 0, byteCount)
                    }
                    inputStream.close()
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun copyAsset(context: Context) {
        try {
            val fileNames = context.assets.list("files")
            val files = File(Environment.getExternalStorageDirectory(), "files")
            for (fileName in fileNames!!) {
                val file = File(files, fileName)
                Log.d("FileUtils", "copyAsset: " + file.absolutePath)
                if (file.canWrite()) {
                    val inputStream = context.assets.open("files/$fileName")
                    val outputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var byteCount: Int
                    while (inputStream.read(buffer).also { byteCount = it } != -1) {
                        outputStream.write(buffer, 0, byteCount)
                    }
                    inputStream.close()
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}

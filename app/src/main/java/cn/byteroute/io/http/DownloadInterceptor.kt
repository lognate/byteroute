package cn.byteroute.io.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class DownloadInterceptor(file: File, listener: (Long, Long, String) -> Unit) : Interceptor {
    var total: Long = -1
    var progress: Long = 0
    var apkFile = file
    var progressListener = listener

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val realResponse: Response = chain.proceed(chain.request())
        val responseBody = realResponse.body
        responseBody?.apply {
            total = contentLength()
            apkFile?.apply {
                val fileReader = ByteArray(4096)
                val inputStream = responseBody.byteStream()
                val outputStream: OutputStream = FileOutputStream(this)
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    outputStream.flush()
                    progress += read?.toLong()
                    if (total > 0) {
                        progressListener?.invoke(progress, total, apkFile.absolutePath)
                    }
                }
                outputStream.flush()
            }
        }
        return realResponse
    }
}
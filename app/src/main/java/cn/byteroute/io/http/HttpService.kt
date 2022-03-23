package cn.byteroute.io.http

import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface HttpService {
    companion object {
        fun <T : HttpService> instance(
            clazz: Class<T>,
            okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build(), factory: Converter.Factory = GsonConverterFactory.create()
        ): T {
            return Retrofit.Builder()
                .baseUrl("https://outrange.club")
                .addConverterFactory(factory)
                .client(okHttpClient).build()
                .create(clazz)
        }
    }
}
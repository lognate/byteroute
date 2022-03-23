package cn.byteroute.io.http

import cn.byteroute.io.data.Response
import cn.byteroute.io.data.UpdateInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OutrangeService :HttpService{

    @GET("/update.json")
    suspend fun getUpdate(): Response<UpdateInfo>

    @GET
    @Streaming
    fun downLoadApk(@Url url: String?): Call<ResponseBody>
}
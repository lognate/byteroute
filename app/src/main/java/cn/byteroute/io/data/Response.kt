package cn.byteroute.io.data

data class Response<T>(val code: Int, val msg: String, val data: T)

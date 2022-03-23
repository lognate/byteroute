package cn.byteroute.io

object ByteNative {
    init {
        System.loadLibrary("byteroute")
    }
    external fun startClient(config:String)
    external fun stopClient()
}
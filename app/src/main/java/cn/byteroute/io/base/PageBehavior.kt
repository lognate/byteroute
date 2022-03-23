package cn.byteroute.io.base

import android.content.Intent

interface PageBehavior {
    fun loading()
    fun close()
    fun toast(msg: String)
    fun finish()
    fun startActivity(intent: Intent)
}
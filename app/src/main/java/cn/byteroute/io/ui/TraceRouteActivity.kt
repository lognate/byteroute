package cn.byteroute.io.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.utils.Traceroute
import kotlinx.android.synthetic.main.activity_trace_route.*

class TraceRouteActivity : BaseActivity<BaseViewModel>() {

    lateinit var traceroute: Traceroute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trace_route)
        tv_result.setMovementMethod(ScrollingMovementMethod.getInstance())
        toolbar.setNavigationOnClickListener { finish() }
        var remoteAddr = intent.getStringExtra("remoteAddr")
        var remoteName = intent.getStringExtra("remoteName")
        tv_result.text = remoteName.plus("\n")
        if (remoteAddr.isNullOrEmpty()) {
            tv_result.append("remoteAddress is null")
        } else {
            traceroute = Traceroute(remoteAddr)
            traceroute.start { code, result ->
                tv_result.append(result.plus("\n"))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        traceroute?.stop()
    }
}
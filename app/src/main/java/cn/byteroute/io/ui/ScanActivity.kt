package cn.byteroute.io.ui

import android.os.*
import androidx.annotation.RequiresApi
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel

class ScanActivity : BaseActivity<BaseViewModel>() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
    }
}
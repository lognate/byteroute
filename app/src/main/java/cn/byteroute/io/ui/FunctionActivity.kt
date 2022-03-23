package cn.byteroute.io.ui

import android.os.Bundle
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel

class FunctionActivity : BaseActivity<BaseViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_function)
    }
}
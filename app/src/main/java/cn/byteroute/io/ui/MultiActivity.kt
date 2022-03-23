package cn.byteroute.io.ui

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import cn.byteroute.io.R
import cn.byteroute.io.adapter.MultAdater
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import kotlinx.android.synthetic.main.activity_multi.*

class MultiActivity : BaseActivity<BaseViewModel>() {

    private val adapter by lazy {
        MultAdater()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        /*adapter.addData(FuncData(R.drawable.icon_scan, "二维码", "生成或扫描二维码"))
        adapter.addData(FuncData(R.drawable.icon_scan, "文件分享", "启用HTTP服务分享文件"))
        adapter.addData(FuncData(R.drawable.icon_scan, "连通性测试", "数据到服务器时间"))
        adapter.addData(FuncData(R.drawable.icon_scan, "路由跟踪", "数据经过路由的情况"))
        adapter.addData(FuncData(R.drawable.icon_scan, "数据互转", "json和yaml相互转换"))*/
    }
}
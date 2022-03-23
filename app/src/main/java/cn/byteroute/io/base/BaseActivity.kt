package cn.byteroute.io.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.byteroute.io.data.Action
import cn.byteroute.io.ext.clazz
import cn.byteroute.io.ui.widget.LoadingDialog

open class BaseActivity<T : BaseViewModel> : AppCompatActivity(), PageBehavior {

    val loadDialog by lazy {
        LoadingDialog(this)
    }

    lateinit var viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var clazz = clazz<T>()
        if (clazz != null) {
            viewModel = ViewModelProvider(this).get(clazz)
        }else{
            viewModel = ViewModelProvider(this).get(BaseViewModel::class.java) as T
        }
        viewModel?.bind(this)
        viewModel?.apply {
            actionData.observe(this@BaseActivity, Observer {
                onAction(it)
            })
        }
    }

    open fun onAction(action: Action<*>) {
    }

    override fun onResume() {
        super.onResume()
        viewModel?.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.unBind()
    }

    override fun loading() {
        lifecycleScope.launchWhenResumed { loadDialog.show() }
    }

    override fun close() {
        lifecycleScope.launchWhenResumed { loadDialog.dismiss() }
    }

    override fun toast(msg: String) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }
}
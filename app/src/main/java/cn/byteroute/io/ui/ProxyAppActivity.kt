package cn.byteroute.io.ui

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Key
import cn.byteroute.io.data.AppInfo
import cn.byteroute.io.ext.KeyValue
import com.blankj.utilcode.util.KeyboardUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.activity_proxy_app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ProxyAppActivity : BaseActivity<BaseViewModel>() {
    private val pkgAll: MutableList<String> = ArrayList()
    private val temps: MutableList<AppInfo> = ArrayList()
    private val appInfoAdapter by lazy {
        AppInfoAdapter()
    }
    var appInfos: List<AppInfo> = ArrayList()
    private val keyValue by lazy {
        KeyValue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proxy_app)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val list = keyValue.getList(
            Key.PACKAGE_LIST,
            String::class.java
        )
        if (list != null) {
            pkgAll.addAll(list)
        }
        recyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@ProxyAppActivity, LinearLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(
                DividerItemDecoration(
                    this@ProxyAppActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = appInfoAdapter
        }
        toolbar.setNavigationOnClickListener { finish() }
        GlobalScope.launch(Dispatchers.Main.immediate) {
            loading.visibility = View.VISIBLE
            withContext(Dispatchers.IO) {
                appInfos = queryAppInfo()
            }
            loading.visibility = View.GONE
            appInfoAdapter.addData(appInfos)
        }
        clearSearchIv.setOnClickListener {
            searchEditText.setText("")
            clearSearchIv.visibility = View.INVISIBLE
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (!TextUtils.isEmpty(text)) {
                    clearSearchIv.visibility = View.VISIBLE
                } else {
                    clearSearchIv.visibility = View.INVISIBLE
                }
                temps.clear()
                for (i in appInfos.indices) {
                    val appInfo = appInfos[i]
                    if (appInfo.appLabel.contains(text, true) || appInfo.pkgName.contains(
                            text,
                            true
                        )
                    ) {
                        temps.add(appInfo)
                    }
                }
                appInfoAdapter.setList(temps)
            }

            override fun afterTextChanged(s: Editable) {}
        })
        checkBox1.setOnClickListener {
            pkgAll.clear()
            appInfos.forEach {
                pkgAll.add(it.pkgName)
            }
            appInfoAdapter.notifyDataSetChanged()
        }
        checkBox2.setOnClickListener {
            pkgAll.clear()
            appInfoAdapter.notifyDataSetChanged()
        }
        checkBox3.setOnClickListener {
            var tem = mutableListOf<String>()
            pkgAll.forEach {
                tem.add(it)
            }
            pkgAll.clear()
            appInfos.forEach {
                if (!tem.contains(it.pkgName)) {
                    pkgAll.add(it.pkgName)
                }
            }
            appInfoAdapter.notifyDataSetChanged()
        }
    }

    fun queryAppInfo(): List<AppInfo> {
        val appInfos: MutableList<AppInfo> = ArrayList()
        val pm = this.packageManager // 获得PackageManager对象
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        pm.getInstalledApplications(0)[0].publicSourceDir
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        for (reInfo in resolveInfos) {
            val appInfo = AppInfo()
            appInfo.appLabel = reInfo.loadLabel(pm) as String // 获得应用程序的Label
            appInfo.appIcon = reInfo.loadIcon(pm) // 获得应用程序图标
            appInfo.pkgName = reInfo.activityInfo.packageName.apply {
                if (packageName != this) appInfos.add(appInfo)
            } // 获得应用程序的包名
        }
        return appInfos
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_seach, menu)
        return super.onCreateOptionsMenu(menu)
    }

    inner class AppInfoAdapter :
        BaseQuickAdapter<AppInfo?, BaseViewHolder>(R.layout.item_common, ArrayList()) {
        init {
            setHasStableIds(true)
        }

        override fun convert(holder: BaseViewHolder, appInfo: AppInfo?) {
            holder.getView<MaterialTextView>(R.id.tv_des).text = appInfo?.pkgName
            holder.getView<MaterialTextView>(R.id.tv_text).text = appInfo?.appLabel
            holder.getView<AppCompatImageView>(R.id.item_icon)
                .setBackground(appInfo?.appIcon)
            holder.getView<AppCompatImageView>(R.id.iv_next).visibility = View.GONE
            Log.d("AppInfoAdapter", "convert: ${Arrays.toString(pkgAll.toTypedArray())}")
            var switch = holder.getView<SwitchCompat>(R.id.switc).apply {
                if (pkgAll.contains(appInfo?.pkgName)) {
                    isChecked = true
                } else {
                    isChecked = false
                }
                setOnClickListener {
                    if (isChecked) {
                        pkgAll.add(appInfo?.pkgName ?: "")
                    } else {
                        pkgAll.remove(appInfo?.pkgName)
                    }
                }
            }
            holder.itemView.setOnClickListener {
                val checked = switch.isChecked
                if (checked) {
                    pkgAll.remove(appInfo?.pkgName)
                } else {
                    pkgAll.add(appInfo?.pkgName ?: "")
                }
                switch.isChecked = !checked
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyValue.put(Key.PACKAGE_LIST, pkgAll)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (isSoftInputVisible) {
                hideSoftInput()
                appInfoAdapter.replaceData(appInfos)
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    val isSoftInputVisible: Boolean
        get() = KeyboardUtils.isSoftInputVisible(this)

    fun hideSoftInput() {
        KeyboardUtils.hideSoftInput(this)
    }


}
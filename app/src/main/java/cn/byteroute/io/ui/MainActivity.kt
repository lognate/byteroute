package cn.byteroute.io.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.textview.MaterialTextView
import cn.byteroute.io.IProxyService
import cn.byteroute.io.ProxyConnection
import cn.byteroute.io.R
import cn.byteroute.io.annotation.ProxyState
import cn.byteroute.io.annotation.WebType
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Constants
import cn.byteroute.io.common.Key
import cn.byteroute.io.data.Config
import cn.byteroute.io.data.ItemData
import cn.byteroute.io.ext.*
import cn.byteroute.io.helper.ContextHelper.startProxyService
import cn.byteroute.io.helper.ContextHelper.startWeb
import cn.byteroute.io.helper.ContextHelper.stopProxyService
import cn.byteroute.io.ui.widget.LoadingView
import cn.byteroute.io.ui.widget.SwitchProgress
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.recyclerView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("RestrictedApi")
class MainActivity : BaseActivity<BaseViewModel>(),
    ProxyConnection.Callback {
    var connection = ProxyConnection(true)

    @ProxyState
    var proxyState = ProxyState.STATE_IDLE

    val keyValue by lazy {
        KeyValue(this)
    }
    //val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private var TAG = "MainActivity"
    @SuppressLint("MissingPermission", "UnsafeOptInUsageError")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connection.connectService(this, this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        var mDrawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
       
        mDrawerToggle.syncState()
        drawer_layout.setDrawerListener(mDrawerToggle)
        navigation_view.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                }
                R.id.privacy -> startWeb(
                    this@MainActivity,
                    "https://github.com/ximenGG/Help/blob/main/README.md",
                    "隐私政策",
                    WebType.WEB_URL
                )
            }
            drawer_layout.closeDrawer(Gravity.LEFT)
            true
        }

        val key = stringPreferencesKey("key")

        lifecycleScope.launch {
            dataStore.edit { settings ->
                settings[key] = "string"
            }
        }
        lifecycleScope.launch {
            dataStore.data.map {
                it[key]
            }.collect {
                Log.d(TAG, "onCreate1: $it")
            }

        }

        lifecycleScope.launch {
            preferencesValue(key).collect {
                Log.d(TAG, "onCreate2: $it")
            }
        }


        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.scan4 -> {
                    var intent = Intent(this, MultiActivity::class.java)
                    startActivity(intent)
                }
                R.id.scan5 -> startScan
            }
            true
        }
        bt_connect.singleClick {
            var config = keyValue[Key.CONFIG, Config::class.java]
            if (config == null) {
                toast("请选择服务器")
                return@singleClick
            }
            when (proxyState) {
                ProxyState.STATE_IDLE, ProxyState.STATE_STOPPED -> startProxy()
                ProxyState.STATE_CONNECTED -> stopProxyService(this)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + getPackageName())
                startActivityForResult(intent, Constants.FILES_MANAGE_CODE)
            }
        }
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ), Constants.REQUEST_PERMISSION_CODE
        )
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu != null) {
            if (menu is MenuBuilder) {
                menu.setOptionalIconsVisible(true)
            }
        }
        return super.onMenuOpened(featureId, menu)
    }

    fun startProxy() {
        val prepare = VpnService.prepare(this)
        if (prepare != null) {
            startActivityForResult(prepare, Constants.START_SERVICE_CODE)
        } else {
            startProxyService(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.START_SERVICE_CODE) {
            if (data == null) {
                startProxyService(this)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_right, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onServiceConnected(service: IProxyService?) {
        try {
            proxyState = service?.state!!
            setState(proxyState)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onServiceDisconnected() {
    }

    override fun onStateChanged(state: Int, msg: String?) {
        proxyState = state
        setState(state)
    }

    fun setState(state: Int) {
        when (state) {
            ProxyState.STATE_CONNECTING -> {
                ringProgress.setStatus(SwitchProgress.Status.CONNECTTING)
                bt_connect.text = "连接中..."
            }
            ProxyState.STATE_CONNECTED -> {
                ringProgress.setStatus(SwitchProgress.Status.CONNETED)
                bt_connect.text = "断开"
                /* var config = keyValue[Key.CONFIG, Config::class.java]
                 CookieBar.build(this)
                     .setLayoutGravity(Gravity.CENTER)
                     .setTitle(
                         "服务器:".plus(
                             when {
                                 config.remoteRemark.isNullOrEmpty() -> config.remoteAddr
                                 else -> config.remoteRemark
                             }
                         )
                     )
                     .setMessage("http代理:http://${localIP()[0]}:2021" + "\nsocks5代理:socks5://${localIP()[0]}:2021")
                     .setAction("确定") {}
                     .setEnableAutoDismiss(false)
                     .setSwipeToDismiss(false)
                     .show()*/
            }
            ProxyState.STATE_IDLE, ProxyState.STATE_STOPPING, ProxyState.STATE_STOPPED -> {
                ringProgress!!.setStatus(SwitchProgress.Status.UNCONNETED)
                bt_connect.text = "连接"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        var config = keyValue[Key.CONFIG, Config::class.java]
        if (config != null) {
            config.apply {
                if (remoteRemark != null) {
                    textArray[0].text = remoteRemark!!
                } else if (remoteAddr != null) {
                    textArray[0].text = remoteAddr
                } else {
                    textArray[0].text = "选择服务器"
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconService(this)
    }

    override fun onBinderDied() {
        connection.disconService(this)
        connection.connectService(this, this)
    }

    private val textArray by lazy {
        arrayListOf<ItemData>(
            ItemData("选择服务器", "", R.drawable.ic_icon_list, true, false, true),
            ItemData("连通性测试", "", R.drawable.ic_connect_test, true, true, true, itemType = 2),
            ItemData("路由分析", "", R.drawable.ic_router, true, false, true),
            ItemData("设置中心", "", R.drawable.ic_settings, true, false, true),
            ItemData("文档", "", R.drawable.ic_document, true, false, true),
        )
    }

    private val adapter by lazy {
        Adapter()
    }

    inner class Adapter :
        BaseMultiItemQuickAdapter<ItemData, BaseViewHolder>() {
        init {
            addItemType(Constants.ITEM_TYPE_1, R.layout.item_common)
            addItemType(Constants.ITEM_TYPE_2, R.layout.item_loading)
            setHasStableIds(true)
            addData(textArray)
        }

        override fun convert(holder: BaseViewHolder, item: ItemData) {
            holder.setIsRecyclable(false)
            holder.getView<AppCompatImageView>(R.id.item_icon)
                .setBackgroundResource(item.drawableId)
            holder.getView<MaterialTextView>(R.id.tv_text).text = item.text
            holder.getView<MaterialTextView>(R.id.tv_des).visibility =
                if (item.des) View.GONE else View.VISIBLE
            if (item.itemType == Constants.ITEM_TYPE_1) {
                holder.getView<SwitchCompat>(R.id.switc).visibility =
                    if (item.switc) View.GONE else View.VISIBLE
                holder.getView<AppCompatImageView>(R.id.iv_next).visibility =
                    if (item.next) View.GONE else View.VISIBLE
            }
            when (holder.position) {
                0 -> {
                    holder.itemView.singleClick {
                        startActivity(Intent(this@MainActivity, SelectActivity::class.java))
                    }
                }
                1 -> {
                    holder.itemView.singleClick {
                        holder.getView<LoadingView>(R.id.loading).visibility = View.VISIBLE
                        holder.getView<MaterialTextView>(R.id.test_text).visibility = View.GONE
                        lifecycleScope.launch(Dispatchers.IO) {
                            var config = keyValue[Key.CONFIG, Config::class.java]
                            if (config != null) {
                                var ping = when (keyValue[Key.PING_MODE, 0]) {
                                    0 -> config.remoteAddr.ping
                                    1 -> tcpPing(config.remoteAddr, config.remotePort)
                                    2 -> Constants.TEST_URL.proxyPing
                                    else -> ""
                                }
                                withContext(Dispatchers.Main.immediate) {
                                    holder.getView<LoadingView>(R.id.loading).visibility = View.GONE
                                    holder.getView<MaterialTextView>(R.id.test_text).visibility =
                                        View.VISIBLE
                                    holder.getView<MaterialTextView>(R.id.test_text).text =
                                        if (ping == -1) {
                                            "--"
                                        } else {
                                            ping.toString().plus(" ms")
                                        }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    holder.itemView.singleClick {
                        var config = keyValue[Key.CONFIG, Config::class.java]
                        Intent(this@MainActivity, TraceRouteActivity::class.java).apply {
                            if (config == null) {
                                toast("请选择服务器")
                                return@singleClick
                            }
                            putExtra("remoteAddr", config.remoteAddr)
                            putExtra("remoteName", config.remoteRemark)
                            startActivity(this)
                        }
                    }
                }
                3 -> {
                    holder.itemView.singleClick {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }
                }
            }
        }
    }

}

package cn.byteroute.io.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.textview.MaterialTextView
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Key
import cn.byteroute.io.data.ItemData
import cn.byteroute.io.ext.KeyValue
import cn.byteroute.io.ext.singleClick
import cn.byteroute.io.helper.NotificationHelper
import kotlinx.android.synthetic.main.activity_settings.recyclerView
import kotlinx.android.synthetic.main.activity_settings.toolbar

class SettingsActivity : BaseActivity<BaseViewModel>() {
    private val keyValue by lazy {
        KeyValue(this)
    }
    private val adapter by lazy {
        Adapter()
    }

    private val itemArray by lazy {
        arrayListOf<ItemData>(
            ItemData("实时监测通知", "通知栏会实时显示网络速度", R.drawable.ic_notify, next = true),//0
            ItemData("应用过滤", "设置应用程序绕过代理", R.drawable.ic_proxy_all, switc = true),//2
            ItemData("VPN热点", "通过热点共享VPN通道", R.drawable.ic_icon_ap),//3
            ItemData("自动断开", "屏幕熄灭时自动断开连接", R.drawable.ic_close, next = true),//4
            ItemData("4/5G热点分享", "4/5G通过热点共享VPN通道", R.drawable.ic_mobile_ap, next = true),//5
            ItemData("域名/ip过滤", "绕过常见的域名或ip", R.drawable.ic_host_filter, next = true),//6
            ItemData("ipv6", "开启Ipv6(需要支持ipv6)", R.drawable.ic_ipv6, next = true),//7
            ItemData("连接测试模式", "TCP", R.drawable.ic_connect_test, switc = true, next = true)//8
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        toolbar.setNavigationOnClickListener { finish() }
        keyValue[Key.VPN_AP, false].apply {
            itemArray[2].checked = this
            adapter.notifyItemChanged(2)
        }
        keyValue[Key.SCREEN_OFF, false].apply {
            itemArray[3].checked = this
            adapter.notifyItemChanged(3)
        }
        keyValue[Key.AP_SHARE, false].apply {
            itemArray[4].checked = this
            adapter.notifyItemChanged(4)
        }
        keyValue[Key.BYPASS_ROUTE, false].apply {
            itemArray[5].checked = this
            adapter.notifyItemChanged(5)
        }
        keyValue[Key.ENABLE_IPV6, false].apply {
            itemArray[6].checked = this
            adapter.notifyItemChanged(6)
        }
        keyValue[Key.PING_MODE, 0].apply {
            itemArray[7].desText = resources.getStringArray(R.array.ping_mode)[this]
        }
    }

    override fun onResume() {
        super.onResume()
        itemArray[0].checked = NotificationHelper.isOpen(this)
        adapter.notifyItemChanged(0)
    }


    inner class Adapter : BaseQuickAdapter<ItemData, BaseViewHolder>(R.layout.item_common) {
        override fun convert(holder: BaseViewHolder, item: ItemData) {
            var switch = holder.getView<SwitchCompat>(R.id.switc).apply {
                visibility = if (item.switc) View.GONE else View.VISIBLE
                isChecked = item.checked
            }
            holder.getView<MaterialTextView>(R.id.tv_des).apply {
                text = item.desText
                visibility = if (item.des) View.GONE else View.VISIBLE
            }
            holder.getView<MaterialTextView>(R.id.tv_text).text = item.text
            holder.getView<AppCompatImageView>(R.id.iv_next).visibility =
                if (item.next) View.GONE else View.VISIBLE
            holder.getView<AppCompatImageView>(R.id.item_icon)
                .setBackgroundResource(item.drawableId)
            when (holder.position) {
                0 -> {
                    switch.setOnClickListener { NotificationHelper.requestPermission(this@SettingsActivity) }
                }
                1 -> {
                    holder.itemView.setOnClickListener {
                        val intent = Intent(this@SettingsActivity, ProxyAppActivity::class.java)
                        startActivity(intent)
                    }
                }
                2 -> {
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        keyValue.put(Key.VPN_AP, isChecked)
                    }
                }
                3 -> {
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        keyValue.put(Key.SCREEN_OFF, isChecked)
                    }
                }
                4 -> {
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        keyValue.put(Key.AP_SHARE, isChecked)
                    }
                }
                5 -> {
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        keyValue.put(Key.BYPASS_ROUTE, isChecked)
                    }
                }
                6 -> {
                    switch.setOnCheckedChangeListener { buttonView, isChecked ->
                        keyValue.put(Key.ENABLE_IPV6, isChecked)
                    }
                }
                7 -> {
                    holder.itemView.singleClick {
                        MaterialDialog(
                            this@SettingsActivity,
                            BottomSheet(LayoutMode.WRAP_CONTENT)
                        ).show {
                            listItems(R.array.ping_mode) { _, index, text ->
                                keyValue.put(Key.PING_MODE, index)
                                holder.getView<MaterialTextView>(R.id.tv_des).text = text
                            }
                            lifecycleOwner(this@SettingsActivity)
                        }
                    }
                }
            }
        }

        init {
            addData(itemArray)
        }
    }
}
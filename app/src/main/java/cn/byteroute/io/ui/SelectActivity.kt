package cn.byteroute.io.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.textview.MaterialTextView
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.common.Key
import cn.byteroute.io.data.Config
import cn.byteroute.io.ext.*
import cn.byteroute.io.utils.ProfileUtils
import kotlinx.android.synthetic.main.activity_select.recyclerView
import kotlinx.android.synthetic.main.activity_select.toolbar
import kotlinx.coroutines.*

@SuppressLint("RestrictedApi")
class SelectActivity : BaseActivity<BaseViewModel>() {
    private val adapter by lazy {
        SelectAdapter()
    }

    private val keyValue by lazy {
        KeyValue(this)
    }

    private val configArray by lazy {
        ProfileUtils.readConfigArray(this@SelectActivity)
    }

    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        (recyclerView.itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)
        recyclerView.adapter = adapter
        keyValue[Key.CONFIG, Config::class.java]?.apply {
            for (i in configArray.indices) {
                if (this.equals(configArray[i])) {
                    index = i
                    break
                }
            }
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            index = position
            var config = adapter.data[position]
            keyValue.put(Key.CONFIG, config)
            adapter.notifyDataSetChanged()
        }
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.test -> ping()
                R.id.more -> toast("更多")
            }
            true
        }
        ping()
    }

    fun ping() {
        for (i in 0 until adapter.data.size) {
            GlobalScope.launch {
                var ping: Int = adapter.data[i].remoteAddr.ping
                adapter.data[i].ping = ping
                withContext(Dispatchers.Main.immediate) {
                    adapter.notifyItemChanged(i)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_server, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu != null) {
            if (menu is MenuBuilder) {
                menu.setOptionalIconsVisible(true)
            }
        }
        return super.onMenuOpened(featureId, menu)
    }

    inner class SelectAdapter() :
        BaseQuickAdapter<Config, BaseViewHolder>(R.layout.item_layout_ssline) {
        override fun convert(holder: BaseViewHolder, item: Config) {
            holder.getView<MaterialTextView>(R.id.tv_text).text = item.remoteRemark
            holder.getView<AppCompatImageView>(R.id.iv_select).visibility = if (index >= 0) {
                if (index == holder.position) View.VISIBLE else View.INVISIBLE
            } else {
                View.INVISIBLE
            }
            if (item.ping > 0) {
                when {
                    item.ping < 300 -> {
                        holder.getView<TextView>(R.id.testip)
                            .setTextColor(Color.parseColor("#FF66CDAA"))
                        holder.getView<ImageView>(R.id.iv_stauts)
                            .setBackgroundResource(R.drawable.shape_circle_green)
                    }
                    item.ping < 1000 -> {
                        holder.getView<TextView>(R.id.testip)
                            .setTextColor(Color.parseColor("#FFF0E68C"))
                        holder.getView<ImageView>(R.id.iv_stauts)
                            .setBackgroundResource(R.drawable.shape_circle_yellow)
                    }
                    else -> {
                        holder.getView<TextView>(R.id.testip)
                            .setTextColor(Color.parseColor("##AAFF0000"))
                        holder.getView<ImageView>(R.id.iv_stauts)
                            .setBackgroundResource(R.drawable.shape_circle_red)
                    }
                }
                holder.getView<TextView>(R.id.testip).text = item.ping.toString().plus(" ms")
            } else if (item.ping == -1) {
                holder.getView<TextView>(R.id.testip).text = "--"
            }
        }

        init {
            addData(configArray)
            setHasStableIds(true)
        }
    }

}


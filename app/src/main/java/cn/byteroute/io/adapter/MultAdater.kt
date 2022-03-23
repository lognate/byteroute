package cn.byteroute.io.adapter

import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.textview.MaterialTextView
import cn.byteroute.io.R
import cn.byteroute.io.data.FuncData

class MultAdater : BaseQuickAdapter<FuncData, BaseViewHolder>(R.layout.item_function, ArrayList()) {
    init {
        setHasStableIds(true)
    }
    override fun convert(holder: BaseViewHolder, funcData: FuncData) {
        holder.getView<MaterialTextView>(R.id.mtv_title).text = funcData.title
        holder.getView<MaterialTextView>(R.id.mtv_des).apply {
            text = funcData.des
            isSelected = true
        }
        holder.getView<AppCompatImageView>(R.id.iv_icon).setBackgroundResource(funcData.drawable)
    }

}
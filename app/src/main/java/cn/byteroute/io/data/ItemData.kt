package cn.byteroute.io.data

import androidx.annotation.DrawableRes
import com.chad.library.adapter.base.entity.MultiItemEntity

data class ItemData(
    var text: String = "",
    var desText: String = "",
    @DrawableRes val drawableId: Int = 0,
    var switc: Boolean = false,
    var next: Boolean = false,
    var des: Boolean = false,
    var checked: Boolean = false,
    var var1: String = "",
    override val itemType: Int=1
) : MultiItemEntity
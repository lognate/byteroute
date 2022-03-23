package cn.byteroute.io.ui.widget

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialDialogs
import cn.byteroute.io.R

/**
 *
 * @ProjectName:    OutrangeApp
 * @Package:        cn.byteroute.io.ui.widget
 * @ClassName:
 * @Description:
 * @Author:         Heqing
 * @CreateDate:     2021-09-11 14:05
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version:
 */
class LoadingDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.layout_loading)
        window?.apply {
            setDimAmount(0f)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        setCancelable(false)
    }
}
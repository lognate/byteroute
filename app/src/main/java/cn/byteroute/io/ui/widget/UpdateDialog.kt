package cn.byteroute.io.ui.widget

import android.app.Dialog
import android.content.Context
import cn.byteroute.io.R

class UpdateDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.dialog_update)
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        setCancelable(false)
    }
}
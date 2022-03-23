package cn.byteroute.io.data

import android.graphics.drawable.Drawable

data class AppInfo(
    var appIcon: Drawable? = null,
    var appLabel: String = "",
    var pkgName: String = "",
) {
    fun equals(obj: Any): Boolean? {
        return when (obj) {
            this == obj -> true
            obj as AppInfo -> obj.pkgName == this.pkgName
            else -> false
        }
    }
}

package cn.byteroute.io.data

data class UpdateInfo(
    var downloadUrl: String,
    var force: Boolean,
    var updateContent: String,
    var versionCode: Int,
    var versionName: String,
    var version: String = "1.0.3",
    var weight: String = "43.67M",
    var updateDate: String = "2021-09-16"
)
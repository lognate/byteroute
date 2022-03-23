package cn.byteroute.io.data

data class TConfig(
    val local_addr: String,
    val local_port: Int,
    val log_file: String,
    val log_level: Int,
    val password: Array<String>,
    val remote_addr: String,
    val remote_port: Int,
    val run_type: String
)
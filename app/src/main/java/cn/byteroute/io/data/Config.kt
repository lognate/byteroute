package cn.byteroute.io.data

data class Config(
    var password: String,
    var remoteAddr: String,
    var remotePort: Int,
    var remoteRemark: String?,
    var ping: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (other is Config) {
            return password == other.password && remoteAddr == other.remoteAddr && remotePort == other.remotePort
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return "Config(password='$password', remoteAddr='$remoteAddr', remotePort=$remotePort, remoteRemark=$remoteRemark)"
    }


}
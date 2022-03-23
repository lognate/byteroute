package cn.byteroute.io.utils

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext

class Traceroute : CoroutineScope {
    var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO+job

    var address: String? = null

    val traceMatcher by lazy {
        Pattern.compile("(?<=From )(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")
    }

    val timeMatcher by lazy {
        Pattern.compile("(?<=time=).*?ms")
    }

    val ipMatcher by lazy {
        Pattern.compile("(?<=from ).*(?=: icmp_seq=1 ttl=)")
    }

    constructor(address: String) {
        this.address = address
    }

    fun start(block: (Int, String) -> Unit) {
       launch {
            var times = 1
            var hostAddress = InetAddress.getByName(address).hostAddress
            while (times < 31) {
                val start = System.currentTimeMillis()
                var result = getPingtOutput(times, hostAddress)
                val end = System.currentTimeMillis()
                if (result.isNullOrEmpty()) {
                    block(-1, "NetWork Error")
                    break
                }
                val trace = traceMatcher.matcher(result)
                val buffer = StringBuffer()
                buffer.append(times).append(".")
                if (trace.find()) {
                    val pingIp = getIpFromTraceMatcher(trace)
                    buffer.append(pingIp?.trim())
                    buffer.append("\t\t")
                    buffer.append((end - start)/2) // 近似值
                    buffer.append("ms\t")
                    block(1, buffer.toString())
                } else {
                    val matchPingIp = ipMatcher.matcher(result)
                    if (matchPingIp.find()) {
                        val pingIp = matchPingIp.group()
                        val matcherTime = timeMatcher.matcher(result)
                        if (matcherTime.find()) {
                            val time = matcherTime.group()
                            buffer.append(pingIp.trim())
                            buffer.append("\t\t")
                            buffer.append(time)
                            buffer.append("\t")
                            block(1, buffer.toString())
                        }
                        break
                    } else {
                        buffer.append("********************")
                        block(-1, buffer.toString())
                    }
                }
                times++
            }
        }
    }

    private fun getPingtOutput(times: Int, host: String): String? {
        var process = Runtime.getRuntime().exec("ping -n -c 1 -t $times $host")
        val reader = BufferedReader(
            InputStreamReader(
                process.inputStream
            )
        )
        var line: String?
        val text = StringBuilder()
        try {
            while (reader.readLine().also { line = it } != null) {
                text.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            process.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        process.destroy()
        return text.toString()
    }

    fun stop() {
        job.cancel()
    }

    fun getIpFromTraceMatcher(m: Matcher): String? {
        var pingIp = m.group()
        val start = pingIp.indexOf('(')
        if (start >= 0) {
            pingIp = pingIp.substring(start + 1)
        }
        return pingIp
    }
}
package io.github.corbant.resonancelauncher.data.server

import android.content.Context
import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

class SetupServerManager(private val context: Context) {
    private var server: EmbeddedServer<*, *>? = null
    val serverPort = 8080

    suspend fun startServer(onConfigReceived: (String, String) -> Unit): String =
        withContext(Dispatchers.IO) {
            val ip = getLocalIpAddress() ?: "127.0.0.1"

            if (server == null) {
                server = embeddedServer(
                    factory = CIO,
                    port = serverPort,
                    parentCoroutineContext = Dispatchers.IO + SupervisorJob()
                ) {
                    routing {
                        get("/") {
                            val inputStream = context.assets.open("setup.html")
                            val htmlBytes = inputStream.use { it.readBytes() }

                            call.respondBytes(
                                bytes = htmlBytes,
                                contentType = ContentType.Text.Html
                            )
                        }

                        post("/save") {
                            val params = call.receiveParameters()
                            val tmdbKey = params["tmdbApiKey"] ?: ""
                            val traktToken = params["traktToken"] ?: ""
                            onConfigReceived(tmdbKey, traktToken)
                        }
                    }
                }.start(wait = false)
            }
            "http://$ip:$serverPort"
        }

    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
    }

    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
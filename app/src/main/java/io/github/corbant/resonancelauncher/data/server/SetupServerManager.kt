package io.github.corbant.resonancelauncher.data.server

import android.content.Context
import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
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
                    parentCoroutineContext = Dispatchers.IO + SupervisorJob(),
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
                            val tmdbKey = params["tmdb_key"]?.trim() ?: ""
                            val streamingKey = params["streaming_availability_key"]?.trim() ?: ""
                            onConfigReceived(tmdbKey, streamingKey)

                            val tmdbBadgeClass = if (tmdbKey.isNotBlank()) "badge-success" else "badge-empty"
                            val tmdbStatus = if (tmdbKey.isNotBlank()) "Updated" else "Not Changed"
                            val streamingBadgeClass = if (streamingKey.isNotBlank()) "badge-success" else "badge-empty"
                            val streamingStatus = if (streamingKey.isNotBlank()) "Updated" else "Not Changed"

                            val htmlTemplate = context.assets.open("save.html").bufferedReader().use { it.readText() }
                            val responseHtml = htmlTemplate
                                .replace("{{TMDB_BADGE_CLASS}}", tmdbBadgeClass)
                                .replace("{{TMDB_STATUS}}", tmdbStatus)
                                .replace("{{STREAMING_BADGE_CLASS}}", streamingBadgeClass)
                                .replace("{{STREAMING_STATUS}}", streamingStatus)

                            call.respondText(
                                text = responseHtml,
                                contentType = ContentType.Text.Html
                            )
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

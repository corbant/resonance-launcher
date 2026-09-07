package io.github.corbant.resonancelauncher.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import qrcode.QRCode
import qrcode.color.Colors

object QrCodeGenerator {

    suspend fun generateQrBitmap(content: String, size: Int = 25): Bitmap = withContext(
        Dispatchers.Default
    ) {
        val qrBytes =
            QRCode.ofCircles().withColor(Colors.BLACK).withSize(size).build(content).renderToBytes()

        BitmapFactory.decodeByteArray(qrBytes, 0, qrBytes.size)
    }
}
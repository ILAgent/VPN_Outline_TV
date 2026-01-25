package com.ilagent.nativeoutline.data.model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID

@SuppressLint("MissingPermission")
fun Context.getLocalIp(): String? {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val active = cm.activeNetwork ?: return null
    val caps = cm.getLinkProperties(active) ?: return null
    val inet = caps.linkAddresses
        .map { it.address.hostAddress }
        .firstOrNull { it != null && ":" !in it }
    return inet
}

fun genToken(): String = UUID.randomUUID().toString().replace("-", "")

fun Bitmap.toImageBitmap(): ImageBitmap =
    Bitmap.createScaledBitmap(this, width, height, false).asImageBitmap()

fun makeQr(content: String, size: Int = 512): Bitmap {
    val bitMatrix = QRCodeWriter().encode(
        content, BarcodeFormat.QR_CODE, size, size
    )
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) for (y in 0 until size) {
        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
    }
    return bmp
}
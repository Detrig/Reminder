package github.detrig.corporatekanbanboard.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun base64ToCroppedBitmap(base64: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        val original = base64ToBitmap(base64) ?: return null

        val scale: Float = maxOf(
            targetWidth.toFloat() / original.width,
            targetHeight.toFloat() / original.height
        )

        val scaledWidth = (scale * original.width).toInt()
        val scaledHeight = (scale * original.height).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true)

        // Центрируем
        val x = (scaledWidth - targetWidth) / 2
        val y = (scaledHeight - targetHeight) / 2

        return Bitmap.createBitmap(scaledBitmap, x, y, targetWidth, targetHeight)
    }
}
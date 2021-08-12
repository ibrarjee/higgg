package com.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileOutputStream


class ImageRotationUtils
{
    public fun getRightAngleImage(photoPath: String): String? {
        try {
            val ei = ExifInterface(photoPath)
            val orientation =
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            var degree = 0
            degree =
                when (orientation) {
                    ExifInterface.ORIENTATION_NORMAL -> 0
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_UNDEFINED -> 0
                    else -> 90
                }
            return rotateImage(degree, photoPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return photoPath
    }

    private fun rotateImage(degree: Int, imagePath: String): String? {
        if (degree <= 0) {
            return imagePath
        }
        try {
            var b = BitmapFactory.decodeFile(imagePath)
            val matrix = Matrix()
            if (b.width > b.height) {
                matrix.setRotate(degree.toFloat())
                b = Bitmap.createBitmap(
                    b, 0, 0, b.width, b.height,
                    matrix, true
                )
            }
            val fOut = FileOutputStream(imagePath)
            val imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1)
            val imageType = imageName.substring(imageName.lastIndexOf(".") + 1)
            val out = FileOutputStream(imagePath)
            if (imageType.equals("png", ignoreCase = true)) {
                b.compress(Bitmap.CompressFormat.PNG, 100, out)
            } else if (imageType.equals("jpeg", ignoreCase = true) || imageType.equals(
                    "jpg",
                    ignoreCase = true
                )
            ) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            fOut.flush()
            fOut.close()
            b.recycle()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return imagePath
    }
}
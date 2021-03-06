package com.utils

import android.graphics.Bitmap

interface OnSaveBitmap {
    fun onBitmapReady(saveBitmap: Bitmap?)
    fun onFailure(e: Exception?)
}
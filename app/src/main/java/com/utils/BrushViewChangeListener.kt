package com.utils

interface BrushViewChangeListener {
    fun onViewAdd(brushDrawingView: BrushDrawingView?)
    fun onViewRemoved(brushDrawingView: BrushDrawingView?)
    fun onStartDrawing()
    fun onStopDrawing()
}

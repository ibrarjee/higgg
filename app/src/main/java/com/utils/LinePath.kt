package com.utils

import android.graphics.Paint
import android.graphics.Path

internal class LinePath(drawPath: Path?, drawPaints: Paint?) {
    val drawPaint: Paint = Paint(drawPaints)
    val drawPath: Path = Path(drawPath)

}
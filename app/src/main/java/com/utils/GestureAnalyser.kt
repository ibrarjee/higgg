package com.utils

import android.os.SystemClock

import android.view.MotionEvent


class GestureAnalyser @JvmOverloads constructor(
    swipeSlopeIntolerance: Int = 3,
    doubleTapMaxDelayMillis: Int = 500,
    doubleTapMaxDownMillis: Int = 100
) {
    private val initialX = DoubleArray(5)
    private val initialY = DoubleArray(5)
    private val finalX = DoubleArray(5)
    private val finalY = DoubleArray(5)
    private val currentX = DoubleArray(5)
    private val currentY = DoubleArray(5)
    private val delX = DoubleArray(5)
    private val delY = DoubleArray(5)
    private var numFingers = 0
    private var initialT: Long = 0
    private var finalT: Long = 0
    private var currentT: Long = 0
    private var prevInitialT: Long = 0
    private var prevFinalT: Long = 0
    private var swipeSlopeIntolerance = 3
    private val doubleTapMaxDelayMillis: Long
    private val doubleTapMaxDownMillis: Long
    fun trackGesture(ev: MotionEvent) {
        val n = ev.pointerCount
        for (i in 0 until n) {
            initialX[i] = ev.getX(i).toDouble()
            initialY[i] = ev.getY(i).toDouble()
        }
        numFingers = n
        initialT = SystemClock.uptimeMillis()
    }

    fun untrackGesture() {
        numFingers = 0
        prevFinalT = SystemClock.uptimeMillis()
        prevInitialT = initialT
    }

    fun getGesture(ev: MotionEvent): GestureType {
        var averageDistance = 0.0
        for (i in 0 until numFingers) {
            finalX[i] = ev.getX(i).toDouble()
            finalY[i] = ev.getY(i).toDouble()
            delX[i] = finalX[i] - initialX[i]
            delY[i] = finalY[i] - initialY[i]
            averageDistance += Math.sqrt(
                Math.pow(finalX[i] - initialX[i], 2.0) + Math.pow(
                    finalY[i] - initialY[i], 2.0
                )
            )
        }
        averageDistance /= numFingers.toDouble()
        finalT = SystemClock.uptimeMillis()
        val gt = GestureType()
        gt.gestureFlag = calcGesture()
        gt.gestureDuration = finalT - initialT
        gt.gestureDistance = averageDistance
        return gt
    }

    fun getOngoingGesture(ev: MotionEvent): Int {
        for (i in 0 until numFingers) {
            currentX[i] = ev.getX(i).toDouble()
            currentY[i] = ev.getY(i).toDouble()
            delX[i] = finalX[i] - initialX[i]
            delY[i] = finalY[i] - initialY[i]
        }
        currentT = SystemClock.uptimeMillis()
        return calcGesture()
    }

    private fun calcGesture(): Int {
        if (isDoubleTap) {
            return DOUBLE_TAP_1
        }
        if (numFingers == 1) {
            if (-delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])) {
                return SWIPE_1_UP
            }
            if (delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])) {
                return SWIPE_1_DOWN
            }
            if (-delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])) {
                return SWIPE_1_LEFT
            }
            if (delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])) {
                return SWIPE_1_RIGHT
            }
        }
        if (numFingers == 2) {
            if (-delY[0] > swipeSlopeIntolerance * Math.abs(delX[0]) && -delY[1] > swipeSlopeIntolerance * Math.abs(
                    delX[1]
                )
            ) {
                return SWIPE_2_UP
            }
            if (delY[0] > swipeSlopeIntolerance * Math.abs(delX[0]) && delY[1] > swipeSlopeIntolerance * Math.abs(
                    delX[1]
                )
            ) {
                return SWIPE_2_DOWN
            }
            if (-delX[0] > swipeSlopeIntolerance * Math.abs(delY[0]) && -delX[1] > swipeSlopeIntolerance * Math.abs(
                    delY[1]
                )
            ) {
                return SWIPE_2_LEFT
            }
            if (delX[0] > swipeSlopeIntolerance * Math.abs(delY[0]) && delX[1] > swipeSlopeIntolerance * Math.abs(
                    delY[1]
                )
            ) {
                return SWIPE_2_RIGHT
            }
            if (finalFingDist(0, 1) > 2 * initialFingDist(0, 1)) {
                return UNPINCH_2
            }
            if (finalFingDist(0, 1) < 0.5 * initialFingDist(0, 1)) {
                return PINCH_2
            }
        }
        if (numFingers == 3) {
            if (-delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])
                && -delY[1] > swipeSlopeIntolerance * Math.abs(delX[1])
                && -delY[2] > swipeSlopeIntolerance * Math.abs(delX[2])
            ) {
                return SWIPE_3_UP
            }
            if (delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])
                && delY[1] > swipeSlopeIntolerance * Math.abs(delX[1])
                && delY[2] > swipeSlopeIntolerance * Math.abs(delX[2])
            ) {
                return SWIPE_3_DOWN
            }
            if (-delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])
                && -delX[1] > swipeSlopeIntolerance * Math.abs(delY[1])
                && -delX[2] > swipeSlopeIntolerance * Math.abs(delY[2])
            ) {
                return SWIPE_3_LEFT
            }
            if (delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])
                && delX[1] > swipeSlopeIntolerance * Math.abs(delY[1])
                && delX[2] > swipeSlopeIntolerance * Math.abs(delY[2])
            ) {
                return SWIPE_3_RIGHT
            }
            if (finalFingDist(0, 1) > 1.75 * initialFingDist(0, 1)
                && finalFingDist(1, 2) > 1.75 * initialFingDist(1, 2)
                && finalFingDist(2, 0) > 1.75 * initialFingDist(2, 0)
            ) {
                return UNPINCH_3
            }
            if (finalFingDist(0, 1) < 0.66 * initialFingDist(0, 1)
                && finalFingDist(1, 2) < 0.66 * initialFingDist(1, 2)
                && finalFingDist(2, 0) < 0.66 * initialFingDist(2, 0)
            ) {
                return PINCH_3
            }
        }
        if (numFingers == 4) {
            if (-delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])
                && -delY[1] > swipeSlopeIntolerance * Math.abs(delX[1])
                && -delY[2] > swipeSlopeIntolerance * Math.abs(delX[2])
                && -delY[3] > swipeSlopeIntolerance * Math.abs(delX[3])
            ) {
                return SWIPE_4_UP
            }
            if (delY[0] > swipeSlopeIntolerance * Math.abs(delX[0])
                && delY[1] > swipeSlopeIntolerance * Math.abs(delX[1])
                && delY[2] > swipeSlopeIntolerance * Math.abs(delX[2])
                && delY[3] > swipeSlopeIntolerance * Math.abs(delX[3])
            ) {
                return SWIPE_4_DOWN
            }
            if (-delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])
                && -delX[1] > swipeSlopeIntolerance * Math.abs(delY[1])
                && -delX[2] > swipeSlopeIntolerance * Math.abs(delY[2])
                && -delX[3] > swipeSlopeIntolerance * Math.abs(delY[3])
            ) {
                return SWIPE_4_LEFT
            }
            if (delX[0] > swipeSlopeIntolerance * Math.abs(delY[0])
                && delX[1] > swipeSlopeIntolerance * Math.abs(delY[1])
                && delX[2] > swipeSlopeIntolerance * Math.abs(delY[2])
                && delX[3] > swipeSlopeIntolerance * Math.abs(delY[3])
            ) {
                return SWIPE_4_RIGHT
            }
            if (finalFingDist(0, 1) > 1.5 * initialFingDist(0, 1)
                && finalFingDist(1, 2) > 1.5 * initialFingDist(1, 2)
                && finalFingDist(2, 3) > 1.5 * initialFingDist(2, 3)
                && finalFingDist(3, 0) > 1.5 * initialFingDist(3, 0)
            ) {
                return UNPINCH_4
            }
            if (finalFingDist(0, 1) < 0.8 * initialFingDist(0, 1)
                && finalFingDist(1, 2) < 0.8 * initialFingDist(1, 2)
                && finalFingDist(2, 3) < 0.8 * initialFingDist(2, 3)
                && finalFingDist(3, 0) < 0.8 * initialFingDist(3, 0)
            ) {
                return PINCH_4
            }
        }
        return 0
    }

    private fun initialFingDist(fingNum1: Int, fingNum2: Int): Double {
        return Math.sqrt(
            Math.pow(initialX[fingNum1] - initialX[fingNum2], 2.0)
                    + Math.pow(initialY[fingNum1] - initialY[fingNum2], 2.0)
        )
    }

    private fun finalFingDist(fingNum1: Int, fingNum2: Int): Double {
        return Math.sqrt(
            (Math.pow((finalX[fingNum1] - finalX[fingNum2]), 2.0)
                    + Math.pow((finalY[fingNum1] - finalY[fingNum2]), 2.0))
        )
    }

    val isDoubleTap: Boolean
        get() {
            return (initialT - prevFinalT < doubleTapMaxDelayMillis) && (finalT - initialT < doubleTapMaxDownMillis) && (prevFinalT - prevInitialT < doubleTapMaxDownMillis)
        }

    inner class GestureType() {
        var gestureFlag = 0
        var gestureDuration: Long = 0
        var gestureDistance = 0.0
    }

    companion object {
        val DEBUG = true

        // Finished gestures flags
        val SWIPE_1_UP = 11
        val SWIPE_1_DOWN = 12
        val SWIPE_1_LEFT = 13
        val SWIPE_1_RIGHT = 14
        val SWIPE_2_UP = 21
        val SWIPE_2_DOWN = 22
        val SWIPE_2_LEFT = 23
        val SWIPE_2_RIGHT = 24
        val SWIPE_3_UP = 31
        val SWIPE_3_DOWN = 32
        val SWIPE_3_LEFT = 33
        val SWIPE_3_RIGHT = 34
        val SWIPE_4_UP = 41
        val SWIPE_4_DOWN = 42
        val SWIPE_4_LEFT = 43
        val SWIPE_4_RIGHT = 44
        val PINCH_2 = 25
        val UNPINCH_2 = 26
        val PINCH_3 = 35
        val UNPINCH_3 = 36
        val PINCH_4 = 45
        val UNPINCH_4 = 46
        val DOUBLE_TAP_1 = 107

        //Ongoing gesture flags
        val SWIPING_1_UP = 101
        val SWIPING_1_DOWN = 102
        val SWIPING_1_LEFT = 103
        val SWIPING_1_RIGHT = 104
        val SWIPING_2_UP = 201
        val SWIPING_2_DOWN = 202
        val SWIPING_2_LEFT = 203
        val SWIPING_2_RIGHT = 204
        val PINCHING = 205
        val UNPINCHING = 206
        private val TAG = "GestureAnalyser"
    }

    init {
        this.swipeSlopeIntolerance = swipeSlopeIntolerance
        this.doubleTapMaxDownMillis = doubleTapMaxDownMillis.toLong()
        this.doubleTapMaxDelayMillis = doubleTapMaxDelayMillis.toLong()
    }
}
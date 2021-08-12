package com.ui.main.viewModel

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaActionSound
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ioscameraandroidapp.R
import com.google.android.material.tabs.TabLayout
import com.ui.main.view.CameraActivity
import com.ui.main.view.SaveFilesActivity
import com.utils.ImageRotationUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class ViewModelCameraActivity : ViewModel() {
    private var mLastClickTime: Long = 0
    private lateinit var activity: CameraActivity
    private lateinit var preview: Preview
    private lateinit var imageCapture: ImageCapture
    private lateinit var videoCapture: VideoCapture
    private lateinit var cameraSelector: CameraSelector
    private lateinit var camera: Camera
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var x1: Float = 0.0f
    private var y1: Float = 0.0f
    private var x2: Float = 0.0f
    private var y2: Float = 0.0f
    private var finalX: Float = 0.0f
    private var finalY: Float = 0.0f
    private val MIN_DISTANCE = 150
    private var timeLeft: Double = 0.0
    private var MY_ACTIONS: String = "NOT_SELECTED"
    private lateinit var cameraSelected: String
    private lateinit var isVideoRecording: String
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var timer: CountDownTimer

    private val mInterval = 0L
    private var mHandler: Handler? = null
    private var timeInSeconds = 0L
    var cancel_timer: Boolean = false

    val currentImagePath: MutableLiveData<String> = MutableLiveData()

    val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio!! * detector.scaleFactor
            camera?.cameraControl?.setZoomRatio(scale)
            return true
        }
    }

    fun init(activity: CameraActivity) {
        this.activity = activity
    }

    @SuppressLint("ResourceAsColor")
    fun startCameraFullScreen()
    {
        activity.binding.constraintLayout2.setBackgroundColor(Color.parseColor("#000000"))
        activity.binding.overLayerViewSquareTop.visibility = View.GONE
        activity.binding.overLayerViewSquareBottom.visibility = View.GONE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            preview!!.setSurfaceProvider(activity.binding.previewId.surfaceProvider)

            imageCapture = ImageCapture.Builder().apply {
                setTargetAspectRatio(AspectRatio.RATIO_4_3)
            }.build()
            cameraSelector =
                CameraSelector.Builder().apply {
                    requireLensFacing(lensFacing)
                }.build()
            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                activity,
                cameraSelector!!, preview, imageCapture
            )!!
        }, ContextCompat.getMainExecutor(activity))
    }
    @SuppressLint("ResourceAsColor")
    fun startCameraSquare() {
        activity.binding.constraintLayout2.setBackgroundColor(Color.parseColor("#000000"))
        activity.binding.overLayerViewSquareTop.visibility = View.VISIBLE
        activity.binding.overLayerViewSquareBottom.visibility = View.VISIBLE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            preview!!.setSurfaceProvider(activity.binding.previewId.surfaceProvider)

            imageCapture = ImageCapture.Builder().apply {
                setTargetAspectRatio(AspectRatio.RATIO_4_3)
            }.build()
            cameraSelector =
                CameraSelector.Builder().apply {
                    requireLensFacing(lensFacing)
                }.build()
            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                activity,
                cameraSelector!!, preview, imageCapture
            )!!
        }, ContextCompat.getMainExecutor(activity))
    }
    fun myListeners() {
        cameraSelected = "PHOTO"
        isVideoRecording = "NO"

        val scaleGestureDetector = ScaleGestureDetector(activity, listener)
        activity.binding.previewId.setOnTouchListener(View.OnTouchListener setOnTouchListener@{ view: View, motionEvent: MotionEvent ->

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                x1 = motionEvent.x;
                y1 = motionEvent.y;
                MY_ACTIONS = ""
            }
            if (motionEvent.action == MotionEvent.ACTION_UP && x1 == motionEvent.x && y1 == motionEvent.y) {
                MY_ACTIONS = "FOCUS"
                val factory = activity.binding.previewId.meteringPointFactory
                val point = factory.createPoint(motionEvent.x, motionEvent.y)
                val action = FocusMeteringAction.Builder(point).build()
                camera?.cameraControl?.startFocusAndMetering(action)
                animateFocusRing(x1, y1)
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE && motionEvent.pointerCount == 2) {
                MY_ACTIONS = "ZOOM"
                scaleGestureDetector.onTouchEvent(motionEvent)
            }
            if (motionEvent.action == MotionEvent.ACTION_UP && motionEvent.pointerCount == 1 && MY_ACTIONS != "ZOOM") {
                x2 = motionEvent.x;
                y2 = motionEvent.y;

                finalX = x2 - x1
                finalY = y2 - y1
                if (abs(finalX) > abs(finalY)) {
                    if (abs(finalX) > MIN_DISTANCE) {
                        // Left to Right swipe action
                        if (x2 > x1) {
                            if (cameraSelected == "PHOTO") {
                                cameraSelected = "VIDEO"
                                setViewsForVideos()
                            } else if (cameraSelected == "SQUARE") {
                                cameraSelected = "PHOTO"
                                setViewsForFullScreenCamera()
                            }
                            animateLeftToRight()
                        }
                        // Right to left swipe action
                        else {
                            if (cameraSelected == "PHOTO") {
                                cameraSelected = "SQUARE"
                                setViewsForSquareScreenCamera()
                            } else if (cameraSelected == "VIDEO") {
                                if (isVideoRecording == "YES") {
                                    stopVideoRecording()
                                    hideTimerForRecording()
                                }
                                cameraSelected = "PHOTO"
                                setViewsForFullScreenCamera()
                            }
                            animateRightToLeft()
                        }

                    }
                }
                if (abs(finalX) < abs(finalY)) {
                    if (abs(finalY) > MIN_DISTANCE) {
                        // Up to Down swipe action
                        if (y2 > y1) {
                            if(isVideoRecording != "YES")
                            {
                                animateTopToBottom()
                                changeLens()
                            }

                        }

                        // Down to Up swipe action
                        else {
                            if(isVideoRecording != "YES")
                            {
                                animateBottomToTop()
                                changeLens()
                            }
//                            animateBottomToTop()
//                            changeLens()
                        }
                    }
                }

            }
            activity.binding.mainTopMenu.visibility = View.VISIBLE
            activity.binding.subMenuForTimer.visibility = View.GONE
            activity.binding.subMenuForFlash.visibility = View.GONE
            return@setOnTouchListener true
        })
        activity.binding.menuTabsLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    "Photo" -> {
                        if (isVideoRecording == "YES") {
                            stopVideoRecording()
                            hideTimerForRecording()
                        }
                        setViewsForFullScreenCamera()
                        animateLeftToRight()
                        cameraSelected = "PHOTO"
                        startCameraFullScreen()
                        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
                        activity.binding.menuTabsLayout.getTabAt(1)?.select();

                    }
                    "Video" -> {
                        animateRightToLeft()
                        cameraSelected = "VIDEO"
                        setViewsForVideos()
                        startCameraForRecording()
                        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
                        activity.binding.menuTabsLayout.getTabAt(0)?.select();
                    }
                    "Square" -> {
                        if (isVideoRecording == "YES") {
                            stopVideoRecording()
                            hideTimerForRecording()
                        }
                        setViewsForSquareScreenCamera()

                        animateLeftToRight()
                        cameraSelected = "SQUARE"
                        startCameraSquare()
                        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
                        activity.binding.menuTabsLayout.getTabAt(2)?.select();
                    }
                }
                Handler().postDelayed(Runnable {
                }, 1000)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    private fun setViewsForSquareScreenCamera() {
        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
        activity.binding.menuTabsLayout.getTabAt(2)?.select();
        activity.binding.takePhotoBtn.visibility = View.VISIBLE
        activity.binding.startRecordingBtn.visibility = View.GONE
        startCameraSquare()
    }
    private fun setViewsForFullScreenCamera() {
        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
        activity.binding.menuTabsLayout.getTabAt(1)?.select();

        activity.binding.takePhotoBtn.visibility = View.VISIBLE
        activity.binding.startRecordingBtn.visibility = View.GONE

        startCameraFullScreen()
    }
    private fun setViewsForVideos() {
        activity.binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
        activity.binding.menuTabsLayout.getTabAt(0)?.select();

        activity.binding.takePhotoBtn.visibility = View.GONE
        activity.binding.startRecordingBtn.visibility = View.VISIBLE
        activity.binding.overLayerViewSquareTop.visibility = View.GONE
        activity.binding.overLayerViewSquareBottom.visibility = View.GONE

        startCameraForRecording()

        if (activity.isFlashOn == "ON") {
            Handler().postDelayed(Runnable {
                camera?.cameraControl?.enableTorch(true)
            }, 800)
        } else if (activity.isFlashOn == "OFF") {
            Handler().postDelayed(Runnable {
                camera?.cameraControl?.enableTorch(false)
            }, 800)
        }
    }
    private fun animateTopToBottom() {
        activity.binding.previewOverLayer.visibility = View.VISIBLE
        Handler().postDelayed(Runnable {
            activity.binding.previewOverLayer.visibility = View.GONE
        }, 1000)
        var isFirstImage = true
        val objectAnimatorFirst =
            ObjectAnimator.ofFloat(activity.binding.parentPreviewId, View.ROTATION_X, 0f, -90f)
        objectAnimatorFirst.duration = 200
        objectAnimatorFirst.start()

        objectAnimatorFirst.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                // Set image in halfway
                isFirstImage = !isFirstImage
//                    imageView.swapImage(isFirstImage)

                val objectAnimatorSecond =
                    ObjectAnimator.ofFloat(
                        activity.binding.parentPreviewId,
                        View.ROTATION_X,
                        90f,
                        0f
                    )
                objectAnimatorSecond.duration = 200
                objectAnimatorSecond.start()
            }
        })
    }
    private fun animateBottomToTop() {
        activity.binding.previewOverLayer.visibility = View.VISIBLE
        Handler().postDelayed(Runnable {
            activity.binding.previewOverLayer.visibility = View.GONE
        }, 1000)
        var isFirstImage = true
        val objectAnimatorFirst =
            ObjectAnimator.ofFloat(activity.binding.parentPreviewId, View.ROTATION_X, 0f, 90f)
        objectAnimatorFirst.duration = 200
        objectAnimatorFirst.start()

        objectAnimatorFirst.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                // Set image in halfway
                isFirstImage = !isFirstImage
//                    imageView.swapImage(isFirstImage)

                val objectAnimatorSecond =
                    ObjectAnimator.ofFloat(
                        activity.binding.parentPreviewId,
                        View.ROTATION_X,
                        -90f,
                        0f
                    )
                objectAnimatorSecond.duration = 200
                objectAnimatorSecond.start()
            }
        })
    }
    private fun animateRightToLeft() {
        activity.binding.previewOverLayer.visibility = View.VISIBLE
        Handler().postDelayed(Runnable {
            activity.binding.previewOverLayer.visibility = View.GONE
        }, 1000)
        var isFirstImage = true
        val objectAnimatorFirst =
            ObjectAnimator.ofFloat(activity.binding.parentPreviewId, View.ROTATION_Y, 0f, -90f)
        objectAnimatorFirst.duration = 200
        objectAnimatorFirst.start()

        objectAnimatorFirst.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                // Set image in halfway
                isFirstImage = !isFirstImage
//                    imageView.swapImage(isFirstImage)

                val objectAnimatorSecond =
                    ObjectAnimator.ofFloat(
                        activity.binding.parentPreviewId,
                        View.ROTATION_Y,
                        90f,
                        0f
                    )
                objectAnimatorSecond.duration = 200
                objectAnimatorSecond.start()
            }
        })
    }
    private fun animateLeftToRight() {
        activity.binding.previewOverLayer.visibility = View.VISIBLE
        Handler().postDelayed(Runnable {
            activity.binding.previewOverLayer.visibility = View.GONE
        }, 1000)
        var isFirstImage = true
        val objectAnimatorFirst =
            ObjectAnimator.ofFloat(activity.binding.parentPreviewId, View.ROTATION_Y, 0f, 90f)
        objectAnimatorFirst.duration = 200
        objectAnimatorFirst.start()

        objectAnimatorFirst.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)

                // Set image in halfway
                isFirstImage = !isFirstImage
//                    imageView.swapImage(isFirstImage)

                val objectAnimatorSecond =
                    ObjectAnimator.ofFloat(
                        activity.binding.parentPreviewId,
                        View.ROTATION_Y,
                        -90f,
                        0f
                    )
                objectAnimatorSecond.duration = 200
                objectAnimatorSecond.start()
            }
        })
    }
    fun takeImageCamera() {

        if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
            setTimer(activity.timerForCaptureImage.toDouble())
        }
        mLastClickTime = SystemClock.elapsedRealtime();
    }
    private fun takePicture() {
        val photoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "IOSApplication- ${System.currentTimeMillis()}.jpg"
        )
        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture?.takePicture(
            output, ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageCapturedCallback(),
                ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    playSoundForImage()
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    currentImagePath.value = photoFile.absolutePath
                    Log.d(" photoFile.absolutePath", "onImageSaved: " + photoFile.absolutePath)

                    if (cameraSelected == "SQUARE")
                    {
                        val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, savedUri)
                        val cropBitmap = saveMediaToStorage(cropToSquare(bitmap)!!)

                        deleteFile(photoFile.absolutePath)
                    }

                }

                override fun onError(exception: ImageCaptureException) {
//                    Toast.makeText(activity, "image not store ", Toast.LENGTH_SHORT).show()
                    Log.d("exception", exception.message.toString())
                }
            })
    }
    private fun deleteFile(filePath: String) {
        val fDelete = File(filePath)
        if (fDelete.exists())
        {
            if (fDelete.delete())
            {
                println("file Deleted :$filePath")
            } else {
                println("file not Deleted :$filePath")
            }
        }
        else
        {
            println("file not Exists :$filePath")
        }

    }

    fun cropToSquare(bitmap: Bitmap): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = if (height > width) width else height
        val newHeight = if (height > width) height - (height - width) else height
        var cropW = (width - height) / 2
        cropW = if (cropW < 0) 0 else cropW
        var cropH = (height - width) / 2
        cropH = if (cropH < 0) 0 else cropH
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
    }

    fun saveMediaToStorage(bitmap: Bitmap) : Bitmap {
        //Generating a file name
        val filename = "Crop_image_${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            activity?.contentResolver?.also { resolver ->

                //Content resolver will process the contentValues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
//            Toast.makeText(context, "Saved to Photos", Toast.LENGTH_SHORT).show()
        }

        return bitmap
    }
    private fun setTimer(time: Double) {

        activity.binding.timeLeftTV.visibility = View.VISIBLE

        timer = object : CountDownTimer(time.toLong(), 1000) {
            override fun onFinish() {
                activity.binding.timeLeftTV.visibility = View.GONE
                activity.timerIsRunning = "NO"
                activity.binding.previewId.isEnabled = true
                activity.binding.menuTabsLayout.isEnabled = true
                activity.binding.mainTopMenu.visibility = View.VISIBLE

                if (!cancel_timer) {
                    if (cameraSelected == "PHOTO" || cameraSelected == "SQUARE") {
                        if (activity.isFlashOn == "OFF") {
                            imageCapture!!.flashMode = ImageCapture.FLASH_MODE_OFF
                            takePicture()
                        } else {
                            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                showFrontFlash()
                                Handler().postDelayed(Runnable {
                                    takePicture()
                                }, 400)
                            } else {
                                imageCapture!!.flashMode = ImageCapture.FLASH_MODE_ON
                                takePicture()
                            }
                        }
                    } else if (cameraSelected == "VIDEO") {
                        startVideoRecording()
                        isVideoRecording = "YES"
                        showTimerForRecording()
                    }
                }
            }
            override fun onTick(millisUntilFinished: Long) {
                activity.binding.timeLeftTV.text = (millisUntilFinished / 1000).toString()
                activity.timerIsRunning = "YES"
            }
        }

        if (activity.isTimerOn == "OFF" && activity.timerIsRunning == "NO") {
            cancel_timer = false
            activity.binding.timeLeftTV.visibility = View.GONE
            activity.binding.previewId.isEnabled = true
            activity.binding.menuTabsLayout.isEnabled = true
            activity.binding.mainTopMenu.visibility = View.VISIBLE
            timer.onFinish()
        } else {
            if (activity.timerIsRunning == "YES") {
                activity.timerIsRunning = "NO"
                timer.cancel()
                activity.binding.timeLeftTV.visibility = View.GONE
                activity.binding.previewId.isEnabled = true
                activity.binding.menuTabsLayout.isEnabled = true
                activity.binding.mainTopMenu.visibility = View.VISIBLE
                timer.onFinish()
                cancel_timer = true

            } else {
                timer.start()
                cancel_timer = false

                activity.binding.previewId.isEnabled = false
                activity.binding.menuTabsLayout.isEnabled = false
                activity.binding.mainTopMenu.visibility = View.GONE
            }
        }
    }
    private fun showFrontFlash() {
        activity.binding.frontFlash.visibility = View.VISIBLE
        val lp = activity.window.attributes
        lp.screenBrightness = 100.0f / 100.0f
        activity.window.attributes = lp
        Handler().postDelayed({
            activity.binding.frontFlash.visibility = View.GONE
        }, 1000)
    }
    private fun playSoundForImage() {
        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)
    }
    fun videoRecording() {
        if (activity.isTimerOn == "ON" && isVideoRecording == "NO") {
            if (activity.isVideoButtonPressed == "NO") {
                activity.isVideoButtonPressed = "YES"
                setTimer(activity.timerForCaptureImage.toDouble())
            }

        } else if (activity.isTimerOn == "ON" && isVideoRecording == "YES") {
            hideTimerForRecording()
            stopVideoRecording()
            isVideoRecording = "NO"
        } else if (activity.isTimerOn == "OFF" && isVideoRecording == "NO") {
            if (activity.isVideoButtonPressed == "NO") {
                activity.isVideoButtonPressed = "YES"
                setTimer(activity.timerForCaptureImage.toDouble())
            }
        } else if (activity.isTimerOn == "OFF" && isVideoRecording == "YES") {
            hideTimerForRecording()
            stopVideoRecording()
            isVideoRecording = "NO"
        }
    }
    private fun hideTimerForRecording() {
        activity.binding.timerViewVideoRecording.visibility = View.GONE
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE

        activity.binding.timerViewVideoRecording.visibility = View.GONE
        mHandler?.removeCallbacks(mStatusChecker)
    }
    private fun updateStopWatchView(timeInSeconds: Long) {
        val formattedTime = getFormattedStopWatch((timeInSeconds * 1000))
        Log.e("formattedTime", formattedTime)
        activity.binding.timerTextView.text = "00:$formattedTime"
    }
    private fun getFormattedStopWatch(ms: Long): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes"
    }
    private var mStatusChecker: Runnable = object : Runnable {
        override fun run() {
            try {
                timeInSeconds += 1
                Log.e("timeInSeconds", timeInSeconds.toString())
                updateStopWatchView(timeInSeconds)
            } finally {
                mHandler!!.postDelayed(this, mInterval.toLong())
            }
        }
    }
    private fun showTimerForRecording() {
        activity.binding.timerViewVideoRecording.visibility = View.VISIBLE
        activity.binding.flashCamera.visibility = View.VISIBLE
        activity.binding.mainTopMenu.visibility = View.GONE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE

        timeInSeconds = 0L
        mHandler = Handler(Looper.getMainLooper())
        mStatusChecker.run()
    }
    @SuppressLint("RestrictedApi")
    private fun stopVideoRecording() {
        videoCapture?.stopRecording()
        Handler().postDelayed(Runnable {
            activity.isVideoButtonPressed = "NO"
        }, 1000)
        activity.binding.timeLeftTV.visibility = View.GONE
        activity.binding.previewId.isEnabled = true
        activity.binding.menuTabsLayout.isEnabled = true
    }
    @SuppressLint("RestrictedApi")
    private fun startVideoRecording() {
        activity.binding.previewId.isEnabled = false
        activity.binding.menuTabsLayout.isEnabled = true

        val videoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "IOSApplicationVideo- ${System.currentTimeMillis()}.mp4"
        )

        val output = VideoCapture.OutputFileOptions.Builder(videoFile).build()
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        videoCapture?.startRecording(
            output,
            executor,
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    Handler(Looper.getMainLooper()).post {
                        currentImagePath.value = videoFile.toString()
                        Log.d("onVideoSaved", "onVideoSaved: $videoFile")
                    }
                }

                override fun onError(
                    videoCaptureError: Int,
                    message: String,
                    cause: Throwable?
                ) {
                    Handler(Looper.getMainLooper()).post {
//                        Toast.makeText(
//                            activity,
//                            "$videoCaptureError $message",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }
        )
    }
    fun openTimerView() {
        activity.binding.mainTopMenu.visibility = View.GONE
        activity.binding.subMenuForTimer.visibility = View.VISIBLE
        activity.binding.subMenuForFlash.visibility = View.GONE
    }
    fun openFlashMode() {
//        Toast.makeText(activity, "openFlashMode", Toast.LENGTH_SHORT).show()
        activity.binding.mainTopMenu.visibility = View.GONE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.VISIBLE
    }
    fun onTimerOffClick() {
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.turnOffTimer.setTextColor(Color.YELLOW)
        activity.binding.turnOffTimer.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fiveSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fiveSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fifteenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fifteenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.timerForCaptureImage = "0.0"
        activity.isTimerOn = "OFF"
    }
    fun onTimerFiveClick() {
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.turnOffTimer.setTextColor(Color.WHITE)
        activity.binding.turnOffTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fiveSecondTimer.setTextColor(Color.YELLOW)
        activity.binding.fiveSecondTimer.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fifteenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fifteenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.timerForCaptureImage = "5000"
        activity.isTimerOn = "ON"
    }
    fun onTimerTenClick() {
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.turnOffTimer.setTextColor(Color.WHITE)
        activity.binding.turnOffTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fiveSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fiveSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.YELLOW)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.fifteenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fifteenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.timerForCaptureImage = "10000"
        activity.isTimerOn = "ON"
    }
    fun onTimerFifteenClick() {
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.turnOffTimer.setTextColor(Color.WHITE)
        activity.binding.turnOffTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fiveSecondTimer.setTextColor(Color.WHITE)
        activity.binding.fiveSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.tenSecondTimer.setTextColor(Color.WHITE)
        activity.binding.tenSecondTimer.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.fifteenSecondTimer.setTextColor(Color.YELLOW)
        activity.binding.fifteenSecondTimer.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.timerForCaptureImage = "15000"
        activity.isTimerOn = "ON"
    }
    fun onFlashOffClick() {
//        Toast.makeText(activity, "onFlashOffClick", Toast.LENGTH_SHORT).show()
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.flashCamera.setImageResource(R.drawable.flashoff_ic)
        activity.binding.turnOffFlash.setImageResource(R.drawable.flashoff_ic_selected)
        activity.binding.turnOffFlash.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.turnOnAutomaticFlash.setImageResource(R.drawable.flash_auto)
        activity.binding.turnOnAutomaticFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.turnOnFlash.setImageResource(R.drawable.flashon_ic)
        activity.binding.turnOnFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.isFlashOn = "OFF"

        if (cameraSelected == "VIDEO") {
            camera?.cameraControl?.enableTorch(false)
        }
    }
    fun onFlashOnClick() {
//        Toast.makeText(activity, "onFlashOnClick", Toast.LENGTH_SHORT).show()
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.flashCamera.setImageResource(R.drawable.flashon_ic)
        activity.binding.turnOnFlash.setImageResource(R.drawable.ic_flash_on)
        activity.binding.turnOnFlash.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.turnOffFlash.setImageResource(R.drawable.flashoff_ic)
        activity.binding.turnOffFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.turnOnAutomaticFlash.setImageResource(R.drawable.flash_auto)
        activity.binding.turnOnAutomaticFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.isFlashOn = "ON"
        if (cameraSelected == "VIDEO") {
            camera?.cameraControl?.enableTorch(true)
        }
    }
    fun onFlashAutomaticClick() {
//        Toast.makeText(activity, "onFlashAutomaticClick", Toast.LENGTH_SHORT).show()
        activity.binding.mainTopMenu.visibility = View.VISIBLE
        activity.binding.subMenuForTimer.visibility = View.GONE
        activity.binding.subMenuForFlash.visibility = View.GONE
        activity.binding.flashCamera.setImageResource(R.drawable.flash_auto)
        activity.binding.turnOnAutomaticFlash.setImageResource(R.drawable.flash_auto_selected)
        activity.binding.turnOnAutomaticFlash.setBackgroundResource(R.drawable.flashoffdrawable)
        activity.binding.turnOffFlash.setImageResource(R.drawable.flashoff_ic)
        activity.binding.turnOffFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.binding.turnOnFlash.setImageResource(R.drawable.flashon_ic)
        activity.binding.turnOnFlash.setBackgroundResource(R.drawable.md_transparent)
        activity.isFlashOn = "ON"
        if (cameraSelected == "VIDEO") {
            camera?.cameraControl?.enableTorch(true)
        }
    }
    fun openSaveFiles() {
        activity.startActivity(Intent(activity, SaveFilesActivity::class.java))
        activity.finish()
    }
    fun changeLens() {

        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        when (cameraSelected) {
            "PHOTO" -> {
                startCameraFullScreen()
            }
            "SQUARE" -> {
                startCameraSquare()
            }
            "VIDEO" -> {
                setViewsForVideos()
            }
        }
    }
    private fun startCameraForRecording() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            val cameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder().build()
            preview!!.setSurfaceProvider(activity.binding.previewId.surfaceProvider)

            videoCapture = VideoCapture.Builder().apply {
//                setFlashMode(flashMode)
            }.build()

            cameraSelector =
                CameraSelector.Builder().apply {
                    requireLensFacing(lensFacing)
                }.build()
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                activity,
                cameraSelector!!, preview, videoCapture
            )!!

        }, ContextCompat.getMainExecutor(activity))
    }
    private fun animateFocusRing(x: Float, y: Float) {
        val focusRing: ImageView = activity.binding.focusRingCamera

        playSoundForFocus()

        // Move the focus ring so that its center is at the tap location (x, y)
        val width: Int = focusRing.width
        val height: Int = focusRing.height
        focusRing.x = x - width / 2
        focusRing.y = y - height / 2

        // Show focus ring
        focusRing.visibility = View.VISIBLE
        focusRing.alpha = 1f

        // Animate the focus ring to disappear
        focusRing.animate()
            .setStartDelay(500)
            .setDuration(300)
            .alpha(0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animator: Animator?) {
                    focusRing.visibility = View.INVISIBLE
                } // The rest of AnimatorListener's methods.

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
    }
    private fun playSoundForFocus() {
        val sound = MediaActionSound()
        sound.play(MediaActionSound.FOCUS_COMPLETE)
    }
}
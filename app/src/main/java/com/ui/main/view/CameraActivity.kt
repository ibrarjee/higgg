package com.ui.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.ActivityCameraBinding
import com.ui.main.viewModel.ViewModelCameraActivity
import com.utils.imagePathGalley

class CameraActivity : BaseActivity()
{
    lateinit var binding:ActivityCameraBinding
    private var viewModel: ViewModelCameraActivity = ViewModelCameraActivity()
    public lateinit var isFlashOn:String
    public lateinit var isVideoButtonPressed:String
    public lateinit var isImageButtonPressed:String
    public lateinit var timerForCaptureImage:String
    public lateinit var isTimerOn:String
    public lateinit var timerIsRunning:String
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        viewModel.init(this)
        binding.viewModel = viewModel
        initViews()
        myListeners()
    }

    private fun myListeners() {
        binding.gallaryIconId.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also { intent ->
                intent.type = "image/*"
                intent.resolveActivity(this.packageManager)?.also {
                    startActivityForResult(intent, REQUEST_PICK_IMAGE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val uri = data?.data
                imagePathGalley = uri.toString()

//                cameraViewModel.selectedImageGalleryPath.value = imagePathGalley

                moveToFilterActivity()

            } else if (requestCode == REQUEST_PICK_IMAGE) {
                val uri = data?.data
                imagePathGalley = uri.toString()
//                cameraViewModel.selectedImageGalleryPath.value = imagePathGalley
                moveToFilterActivity()
            }
        }
    }

    private fun moveToFilterActivity() {
        startActivity(Intent(this, FilterActivity::class.java))
        finish()
    }

    @SuppressLint("ResourceAsColor")
    private fun initViews()
    {
        viewModel.startCameraFullScreen()
        binding.menuTabsLayout.setTabTextColors(Color.WHITE, Color.YELLOW)
        binding.menuTabsLayout.getTabAt(1)?.select();
        binding.subMenuForTimer.visibility = View.GONE
        binding.subMenuForFlash.visibility = View.GONE
        binding.mainTopMenu.visibility = View.VISIBLE
        binding.timerViewVideoRecording.visibility = View.GONE
        binding.frontFlash.visibility = View.GONE
        binding.previewOverLayer.visibility = View.GONE
        viewModel.myListeners()
        isFlashOn = "OFF"
        timerForCaptureImage = "0.0"
        isTimerOn = "OFF"
        isVideoButtonPressed = "NO"
        isImageButtonPressed = "NO"
        timerIsRunning = "NO"

        viewModel.currentImagePath.observe(this) { currentImagePath ->

            Glide.with(this).load(currentImagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.viewFilesBtn)
        }
    }


}
package com.ui.main.view

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.ActivityFilterBinding
import com.example.ioscameraandroidapp.databinding.FragmentFilterBinding
import com.ui.main.adapters.FilterViewAdapter
import com.ui.main.view.fragments.FilterFragment
import com.ui.main.viewModel.FilterViewModel
import com.utils.*
import com.utils.listeners.FilterListener
import com.utils.listeners.OnPhotoEditorListener
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FilterActivity : BaseActivity() , FilterListener, OnPhotoEditorListener,
    View.OnClickListener
{
    private lateinit var binding: ActivityFilterBinding
    private var filterViewModel:FilterViewModel = FilterViewModel()
    private val mFilterViewAdapter: FilterViewAdapter = FilterViewAdapter(this)
    var mPhotoEditor: PhotoEditor? = null
    private lateinit var mSaveFileHelper: FileSaveHelper
    private val CAMERA_REQUEST = 52
    private val PICK_REQUEST = 53

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter)

        initViews()
        handleIntentImage(binding.imageViewForFilter.source!!)
        myListeners();
    }

    private fun myListeners() {
        binding.imgCamera.setOnClickListener(this)
        binding.imgGallery.setOnClickListener(this)
        binding.imgSave.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgShare.setOnClickListener(this)
    }
    private fun handleIntentImage(source: ImageView) {
        try {
//                    val uri = imagePath.toUri()
            if (imagePathGalley =="")
            {
                val bm: Bitmap = shrinkBitmap(imagePath, 1200, 1200)!!
                source.setImageBitmap(bm)
            }
            else
            {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        this?.contentResolver,
                        Uri.parse(imagePathGalley)
                    )
                    source.setImageBitmap(bitmap)
                } catch (e: java.lang.Exception) {
                    //handle exception
                }
            }

//                    source.setImageURI(uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun shrinkBitmap(file: String?, width: Int, height: Int): Bitmap? {
        val bmpFactoryOptions = BitmapFactory.Options()
        bmpFactoryOptions.inJustDecodeBounds = true
        var bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions)
        val heightRatio =
            Math.ceil((bmpFactoryOptions.outHeight / height.toFloat()).toDouble()).toInt()
        val widthRatio =
            Math.ceil((bmpFactoryOptions.outWidth / width.toFloat()).toDouble()).toInt()
        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                bmpFactoryOptions.inSampleSize = heightRatio
            } else {
                bmpFactoryOptions.inSampleSize = widthRatio
            }
        }
        bmpFactoryOptions.inJustDecodeBounds = false
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte: ByteArray = stream.toByteArray()
        //this gives the size of the compressed image in kb
        val lengthbmp = (imageInByte.size / 1024).toLong()
        try {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                FileOutputStream(imagePath)
            )
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return bitmap
    }
    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor?.setFilterEffect(photoFilter)
        isImageSaved = "TRUE"
    }
    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        TODO("Not yet implemented")
    }
    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        TODO("Not yet implemented")
    }
    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        TODO("Not yet implemented")
    }
    override fun onStartViewChangeListener(viewType: ViewType?) {
        TODO("Not yet implemented")
    }
    override fun onStopViewChangeListener(viewType: ViewType?) {
        TODO("Not yet implemented")
    }
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.imgSave -> filterViewModel.saveImage()
            R.id.imgClose -> filterViewModel.closeThisFragment()
            R.id.imgShare -> filterViewModel.shareImage()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                this.startActivityForResult(
                    cameraIntent,
                    CAMERA_REQUEST
                )
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor!!.clearAllViews()
                    val photo = data!!.extras!!["data"] as Bitmap?
                    binding.imageViewForFilter.source?.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri = data!!.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    binding.imageViewForFilter.source?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun initViews() {
        filterViewModel.init(this)

        mSaveFileHelper = FileSaveHelper(contentResolver)

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilterView.layoutManager = llmFilters
        binding.rvFilterView.adapter = mFilterViewAdapter

        mPhotoEditor = PhotoEditor.Builder(this, binding.imageViewForFilter).build() // build photo editor sdk

        mPhotoEditor!!.setOnPhotoEditorListener(this)
//        mSaveFileHelper = FileSaveHelper(AppCompatActivity())

        isImageSaved = "FALSE"
    }
    override fun onBackPressed() {
        if (isImageSaved == "FALSE")
        {
            startActivity(Intent(this,SaveFilesActivity::class.java))
            finish()
        }
        else
        {
            showAlertForConfirmation()
//            Toast.makeText(this, "Image save first Please.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showAlertForConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Please select your desire option.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Save"){dialogInterface, which ->
            filterViewModel.saveImage()
            dialogInterface.dismiss()
        }
        builder.setNeutralButton("Cancel"){dialogInterface , which ->
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Discard"){dialogInterface, which ->
            startActivity(Intent(this,SaveFilesActivity::class.java))
            finish()
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
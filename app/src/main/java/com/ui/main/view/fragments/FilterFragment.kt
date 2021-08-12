package com.ui.main.view.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.FragmentFilterBinding
import com.ui.main.adapters.FilterViewAdapter
import com.ui.main.view.FilterActivity
import com.ui.main.viewModel.FilterViewModel
import com.utils.*
import com.utils.listeners.FilterListener
import com.utils.listeners.OnPhotoEditorListener
import java.io.*


class FilterFragment : Fragment(), FilterListener, OnPhotoEditorListener,
    View.OnClickListener
{

    private lateinit var binding:FragmentFilterBinding
//    public val filterViewModel: FilterViewModel by activityViewModels()
    private val mFilterViewAdapter: FilterViewAdapter = FilterViewAdapter(this)

    var mPhotoEditor: PhotoEditor? = null
    private var mSaveFileHelper: FileSaveHelper = FileSaveHelper(this.context?.contentResolver)


    @VisibleForTesting


    private val CAMERA_REQUEST = 52
    private val PICK_REQUEST = 53

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        initViews(container)
        handleIntentImage(binding.imageViewForFilter.source!!)
        myListeners();

        return binding.root
    }

    private fun myListeners() {
        binding.imgCamera.setOnClickListener(this)
        binding.imgGallery.setOnClickListener(this)
        binding.imgSave.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgShare.setOnClickListener(this)

    }
    private fun initViews(container: ViewGroup?) {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_filter, container, false)

//        filterViewModel.init(this, requireActivity())


        val llmFilters = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilterView.layoutManager = llmFilters
        binding.rvFilterView.adapter = mFilterViewAdapter

        mPhotoEditor = PhotoEditor.Builder(context, binding.imageViewForFilter)
            .build() // build photo editor sdk

        mPhotoEditor!!.setOnPhotoEditorListener(this)
//        mSaveFileHelper = FileSaveHelper(AppCompatActivity())

        isImageSaved = "FALSE"
    }
    private fun handleIntentImage(source: ImageView) {
                try {
//                    val uri = imagePath.toUri()
                    if (imagePathGalley=="")
                    {
                        val bm: Bitmap = shrinkBitmap(imagePath, 1200, 1200)!!
                        source.setImageBitmap(bm)
                    }
                    else
                    {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                context?.contentResolver,
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
        bitmap.compress(CompressFormat.JPEG, 100, stream)
        val imageInByte: ByteArray = stream.toByteArray()
        //this gives the size of the compressed image in kb
        val lengthbmp = (imageInByte.size / 1024).toLong()
        try {
            bitmap.compress(
                CompressFormat.JPEG,
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
//            R.id.imgSave -> filterViewModel.saveImage()
//            R.id.imgClose -> filterViewModel.closeThisFragment()
//            R.id.imgShare -> filterViewModel.shareImage()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                activity?.startActivityForResult(
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
                    val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
                    binding.imageViewForFilter.source?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun backToSaveList(filterActivity: FilterActivity)
    {
        Toast.makeText(filterActivity, "clicked", Toast.LENGTH_SHORT).show()
//        if (filterViewModel != null)
//        {
//            filterViewModel.saveImage()
//        }
    }
}
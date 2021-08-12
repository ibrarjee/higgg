package com.ui.main.viewModel

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ui.main.view.FilterActivity
import com.ui.main.view.SaveFilesActivity
import com.utils.FileSaveHelper
import com.utils.PhotoEditor
import com.utils.SaveSettings
import com.utils.isImageSaved
import java.io.File

class FilterViewModel : ViewModel()
{
    private lateinit var activity: FilterActivity

    var mSaveImageUri: Uri? = null
    private val FILE_PROVIDER_AUTHORITY = "com.file_provider"

    val imageSaveIndicator: MutableLiveData<String> = MutableLiveData()

    fun init(
        activity: FilterActivity
    ) {
        this.activity = activity
    }
    public fun saveImage() {
        // Create a file to save the image
//        val photoFile = File(
//            activity.externalMediaDirs.firstOrNull(),
//            "IOSApplication- ${System.currentTimeMillis()}.jpg"
//        )

        val photoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "IOSApplication- ${System.currentTimeMillis()}.jpg")


        val hasStoragePermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
//            Toast.makeText(context, "Saving...", Toast.LENGTH_SHORT).show()
            val saveSettings: SaveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build()
            activity.mPhotoEditor!!.saveAsFile(photoFile.toString(), saveSettings, object : PhotoEditor.OnSaveListener{
                override fun onSuccess(imagePath: String) {
//                            mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(context?.contentResolver)
                    mSaveImageUri = photoFile.toUri()
//                            binding.imageViewForFilter.source?.setImageURI(mSaveImageUri)
                    imageSaveIndicator.value = "saved"

                    isImageSaved = "FALSE"

                    Handler().postDelayed(Runnable {
                        closeThisFragment()
                    }, 500)

                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(activity, "Failed to save Image", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(activity, "Please Allowed Permissions", Toast.LENGTH_SHORT).show()
        }
    }


    fun shareImage() {
        if (mSaveImageUri == null) {
//            Toast.makeText(fragmentActivity, getString(R.string.msg_save_image_to_share), Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(mSaveImageUri!!))
        activity.startActivity(Intent.createChooser(intent, "Share Image"))
    }
    private fun buildFileProviderUri(uri: Uri): Uri? {
        return FileProvider.getUriForFile(
            activity,
            FILE_PROVIDER_AUTHORITY,
            File(uri.path)
        )
    }
    fun closeThisFragment() {
        if (isImageSaved == "FALSE")
        {
            activity?.let {
                val intent = Intent(it, SaveFilesActivity::class.java)
                it.startActivity(intent)
                it.finish()
            }
        }
        else
        {
            showAlertForConfirmation()
//            Toast.makeText(context, "Image save first Please.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showAlertForConfirmation() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Alert")
        builder.setMessage("Please select your desire option.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Save"){dialogInterface, which ->
            saveImage()
//            dialogInterface.dismiss()
        }
        builder.setNeutralButton("Cancel"){dialogInterface , which ->
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("Discard"){dialogInterface, which ->
            activity?.let {
                val intent = Intent(it, SaveFilesActivity::class.java)
                it.startActivity(intent)
                it.finish()
            }
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
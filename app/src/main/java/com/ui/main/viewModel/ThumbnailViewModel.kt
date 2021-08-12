package com.ui.main.viewModel

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.ui.main.models.ThumbnailModel
import com.ui.main.view.CameraActivity
import com.ui.main.view.fragments.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ThumbnailViewModel:ViewModel()
{
    var mList = MutableLiveData<ArrayList<ThumbnailModel>>()
    var newList = arrayListOf<ThumbnailModel>()

    private lateinit var saveFragment: ViewSavedFilesFragments
    private lateinit var fragmentActivity: FragmentActivity
    fun init(
        saveFragment: ViewSavedFilesFragments,
        activity: FragmentActivity
    ) {
        this.fragmentActivity = activity
        this.saveFragment = saveFragment;
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    fun getFiles()
    {
//        var gpath: String = Environment.getExternalStorageDirectory().absolutePath
//        var spath = "Android/media/com.example.ioscameraandroidapp/"
//        var spath = "storage/emulated/0/DCIM/"
//        var fullpath = File(gpath + File.separator + spath)
        var fullpath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)


        Log.i("fullpath", "" + fullpath)
        imageReaderNew(fullpath)
    }

    private fun imageReaderNew(root: File):ArrayList<ThumbnailModel>
    {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.isNotEmpty())
        {
            for (currentFile in listAllFiles) {
                    Log.e("FilePath", currentFile.absolutePath)
                    fileList.add(currentFile.absoluteFile)
                }
//            }
            Log.w("fileList", "" + fileList)

            Collections.sort(fileList, Comparator<File?> { o1, o2 ->
                val k = o1.lastModified() - o2.lastModified()
                when {
                    k < 0 -> {
                        1
                    }
                    k == 0L -> {
                        0
                    }
                    else -> {
                        -1
                    }
                }
            })

            for (currentFile in fileList)
            {
                newList.add(ThumbnailModel(currentFile.absolutePath))
            }
        }
        else
        {
            fragmentActivity.startActivity(Intent(fragmentActivity, CameraActivity::class.java))
            fragmentActivity.finish()
        }
        return newList
    }

    fun remove(blog: ThumbnailModel){
        newList.remove(blog)
        mList.value=newList
    }
}
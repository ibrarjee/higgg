package com.ui.main.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.FragmentViewSavedFilesFragmentsBinding
import com.ui.main.adapters.ThumbnailAdapter
import com.ui.main.view.CameraActivity
import com.ui.main.view.FilterActivity
import com.ui.main.viewModel.ThumbnailViewModel
import com.utils.SnapHelperOneByOne
import com.utils.imagePath
import com.utils.imagePathGalley
import java.io.File


class ViewSavedFilesFragments : Fragment()
{
    private lateinit var binding:FragmentViewSavedFilesFragmentsBinding
    private val viewModel: ThumbnailViewModel by activityViewModels()
    private var linearSnapHelper = SnapHelperOneByOne()
    private lateinit var adapter:ThumbnailAdapter
    private val FILE_PROVIDER_AUTHORITY = "com.file_provider"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_view_saved_files_fragments,
            container,
            false)

        initViews()
        mySetListeners()
        return binding.root
    }

    private fun mySetListeners()
    {
        binding.imgShare.setOnClickListener{
            shareFile()
        }

        binding.imgDelete.setOnClickListener{
            showAlertForDelete()
        }

        binding.imgBackBtn.setOnClickListener{
            activity?.let {
                val intent = Intent(it, CameraActivity::class.java)
                it.startActivity(intent)
                it.finish()
            }
        }

        binding.imgInfo.setOnClickListener{
            showAlertForInfo()
        }

        binding.imgEditBtn.setOnClickListener{
            imagePath = viewModel.newList[binding.thumbnailsRecyclerView.getCurrentPosition()].file
            imagePathGalley = ""
            if (imagePath.contains("mp4"))
            {
                Toast.makeText(context, "you can't edit Video file.", Toast.LENGTH_SHORT).show()

            }
            else if (imagePath.contains("jpg"))
            {
                activity?.let {
                    val intent = Intent(it, FilterActivity::class.java)
                    it.startActivity(intent)
                    it.finish()
                }
            }
            else
            {

            }
        }
    }

    private fun shareFile()
    {
        var filePath:String = viewModel.newList[binding.thumbnailsRecyclerView.getCurrentPosition()].file

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(filePath.toUri()!!))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    private fun buildFileProviderUri(uri: Uri): Uri? {
        return FileProvider.getUriForFile(
            requireContext(),
            FILE_PROVIDER_AUTHORITY,
            File(uri.path)
        )
    }

    private fun showAlertForInfo()
    {
        val dialogView = layoutInflater.inflate(R.layout.info_dialouge_layout, null)
        val customDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .show()
        customDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }

    private fun showAlertForDelete()
    {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete File")
        builder.setMessage("Are you sure you want to delete this file.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            deleteFileFromStorage()
            dialogInterface.dismiss()
            var action = ViewSavedFilesFragmentsDirections.
            actionViewSavedFilesFragments2Self()
            findNavController().navigate(action)
        }
        builder.setNeutralButton("Cancel"){dialogInterface , which ->
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No"){dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    private fun deleteFileFromStorage()
    {
        var filePath:String = viewModel.newList[binding.thumbnailsRecyclerView.getCurrentPosition()].file

        val fdelete: File = File(filePath)
        if (fdelete.exists())
        {
            if (fdelete.delete())
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

    private fun initViews()
    {
        viewModel.init(this, requireActivity())

        viewModel.newList.clear()
        initializeAdapter();
        viewModel.getFiles()

    }

    private fun initializeAdapter()
    {
        binding.thumbnailsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.thumbnailsRecyclerView.itemAnimator = DefaultItemAnimator()
        adapter = ThumbnailAdapter(viewModel.newList, requireContext(), this)
        binding.thumbnailsRecyclerView.adapter = adapter
        linearSnapHelper.attachToRecyclerView(binding.thumbnailsRecyclerView)
        adapter.notifyDataSetChanged();

    }

    private fun RecyclerView.getCurrentPosition() : Int {
        return (this.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
    }
}
package com.ui.main.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ioscameraandroidapp.R
import com.ui.main.models.ThumbnailModel
import com.ui.main.view.FilterActivity
import com.ui.main.view.fragments.ViewSavedFilesFragments
import com.utils.imagePath
import kotlinx.android.synthetic.main.thumbnails_item_layout.view.*


class ThumbnailAdapter(
    private val mList: ArrayList<ThumbnailModel>,
    private val mContext: Context,
    viewSavedFilesFragments: ViewSavedFilesFragments
)
    : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>()
{

    var viewSavedFilesFragments = viewSavedFilesFragments

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ThumbnailAdapter.ViewHolder
    {
        var root = LayoutInflater.from(parent.context).inflate(R.layout.thumbnails_item_layout,parent,false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ThumbnailAdapter.ViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        if(mList.size==0){
//            Toast.makeText(mContext,"List is empty", Toast.LENGTH_LONG).show()
        }
        else
        {

        }
        return mList.size
    }
    inner class ViewHolder (var binding:View):RecyclerView.ViewHolder(binding)
    {
        fun bind(model: ThumbnailModel)
        {
            Glide.with(mContext).load(model.file)
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(binding.thumbnails_item_imageview)

            if (model.file.contains("mp4"))
            {
                binding.thumbnails_item_play_btn.visibility = View.VISIBLE
                binding.thumbnails_item_play_btn.isEnabled = true
            }
            else
            {
                binding.thumbnails_item_play_btn.visibility = View.INVISIBLE
                binding.thumbnails_item_play_btn.isEnabled = false
            }

            binding.deleteBtn.setOnClickListener {
            }

            binding.editBtn.setOnClickListener{
                imagePath = model.file
//                val action = ViewSavedFilesFragmentsDirections.actionViewSavedFilesFragmentsToFilterFragment()
//                viewSavedFilesFragments.findNavController().navigate(action)
                mContext.startActivity(Intent(mContext, FilterActivity::class.java))
                (mContext as Activity).finish()
            }

            binding.thumbnails_item_play_btn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(model.file.toUri(), "video/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //DO NOT FORGET THIS EVER
                mContext.startActivity(intent)
            }
            binding.thumbnails_item_imageview.setOnLongClickListener {
//                Toast.makeText(mContext, "Clicked", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }

        }
    }

}
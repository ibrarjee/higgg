package com.ui.main.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ioscameraandroidapp.R
import com.utils.PhotoFilter
import com.utils.listeners.FilterListener
import java.io.IOException
import java.io.InputStream
import java.util.*

class FilterViewAdapter(filterListener: FilterListener) :
    RecyclerView.Adapter<FilterViewAdapter.ViewHolder>() {
    private val mFilterListener: FilterListener = filterListener
    private val mPairList: MutableList<Pair<String, PhotoFilter>> = ArrayList<Pair<String, PhotoFilter>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.row_filter_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val filterPair: Pair<String, PhotoFilter> = mPairList[position]
        val fromAsset = getBitmapFromAsset(holder.itemView.context, filterPair.first)
        holder.mImageFilterView.setImageBitmap(fromAsset)
        holder.mTxtFilterName.text = filterPair.second.name.replace("_", " ")
    }

    override fun getItemCount(): Int {
        return mPairList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImageFilterView: ImageView = itemView.findViewById(R.id.imgFilterView)
        var mTxtFilterName: TextView = itemView.findViewById(R.id.txtFilterName)

        init {
            itemView.setOnClickListener { mFilterListener.onFilterSelected(mPairList[layoutPosition].second) }
        }
    }

    private fun getBitmapFromAsset(context: Context, strName: String): Bitmap? {
        val assetManager = context.assets
        var istr: InputStream? = null
        return try {
            istr = assetManager.open(strName)
            BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setupFilters() {
        mPairList.add(Pair<String, PhotoFilter>("filters/original.jpg", PhotoFilter.NONE))
        mPairList.add(Pair<String, PhotoFilter>("filters/auto_fix.png", PhotoFilter.AUTO_FIX))
        mPairList.add(Pair<String, PhotoFilter>("filters/brightness.png", PhotoFilter.BRIGHTNESS))
        mPairList.add(Pair<String, PhotoFilter>("filters/contrast.png", PhotoFilter.CONTRAST))
        mPairList.add(Pair<String, PhotoFilter>("filters/documentary.png", PhotoFilter.DOCUMENTARY))
        mPairList.add(Pair<String, PhotoFilter>("filters/dual_tone.png", PhotoFilter.DUE_TONE))
        mPairList.add(Pair<String, PhotoFilter>("filters/fill_light.png", PhotoFilter.FILL_LIGHT))
        mPairList.add(Pair<String, PhotoFilter>("filters/fish_eye.png", PhotoFilter.FISH_EYE))
        mPairList.add(Pair<String, PhotoFilter>("filters/grain.png", PhotoFilter.GRAIN))
        mPairList.add(Pair<String, PhotoFilter>("filters/gray_scale.png", PhotoFilter.GRAY_SCALE))
        mPairList.add(Pair<String, PhotoFilter>("filters/lomish.png", PhotoFilter.LOMISH))
        mPairList.add(Pair<String, PhotoFilter>("filters/negative.png", PhotoFilter.NEGATIVE))
        mPairList.add(Pair<String, PhotoFilter>("filters/posterize.png", PhotoFilter.POSTERIZE))
        mPairList.add(Pair<String, PhotoFilter>("filters/saturate.png", PhotoFilter.SATURATE))
        mPairList.add(Pair<String, PhotoFilter>("filters/sepia.png", PhotoFilter.SEPIA))
        mPairList.add(Pair<String, PhotoFilter>("filters/sharpen.png", PhotoFilter.SHARPEN))
        mPairList.add(Pair<String, PhotoFilter>("filters/temprature.png", PhotoFilter.TEMPERATURE))
        mPairList.add(Pair<String, PhotoFilter>("filters/tint.png", PhotoFilter.TINT))
        mPairList.add(Pair<String, PhotoFilter>("filters/vignette.png", PhotoFilter.VIGNETTE))
        mPairList.add(Pair<String, PhotoFilter>("filters/cross_process.png", PhotoFilter.CROSS_PROCESS))
        mPairList.add(Pair<String, PhotoFilter>("filters/b_n_w.png", PhotoFilter.BLACK_WHITE))
        mPairList.add(Pair<String, PhotoFilter>("filters/flip_horizental.png", PhotoFilter.FLIP_HORIZONTAL))
        mPairList.add(Pair<String, PhotoFilter>("filters/flip_vertical.png", PhotoFilter.FLIP_VERTICAL))
        mPairList.add(Pair<String, PhotoFilter>("filters/rotate.png", PhotoFilter.ROTATE))
    }

    init {
        setupFilters()
    }
}
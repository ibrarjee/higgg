package com.ui.main.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.FragmentViewSavedFilesFragmentsBinding
import com.ui.main.adapters.ThumbnailAdapter
import com.ui.main.viewModel.ThumbnailViewModel
import com.utils.SnapHelperOneByOne

class SaveFilesActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_files)
    }

    override fun onBackPressed()
    {
        startActivity(Intent(this, CameraActivity::class.java))
        finish()
    }
}
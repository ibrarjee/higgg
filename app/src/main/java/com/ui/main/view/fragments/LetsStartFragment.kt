package com.ui.main.view.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.FragmentLetsStartBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ui.main.view.CameraActivity
import com.ui.main.view.SplashActivity


class LetsStartFragment : Fragment() {
    private lateinit var binding: FragmentLetsStartBinding
    private val permissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_lets_start, container, false)
        showPermissionAlert()
        binding.letsStartBtn.setOnClickListener {
            startActivity(Intent(context,CameraActivity::class.java))
            (context as SplashActivity).finish()
        }
        return binding.root
    }
//            03129920616 papo property dealer...
    private fun showPermissionAlert() {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAPTURE_AUDIO_OUTPUT,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    binding.letsStartBtn.visibility = View.VISIBLE

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }
}
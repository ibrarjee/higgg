package com.ui.main.view.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.ioscameraandroidapp.R
import com.example.ioscameraandroidapp.databinding.FragmentSplashBinding

class SplashFragment : Fragment()
{
    private lateinit var binding:FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_splash, container, false)

        initViews()


        return binding.root
    }

    private fun initViews() {
        Handler().postDelayed(Runnable {
            var action = SplashFragmentDirections.actionSplashFragmentToLetsStartFragment()
            findNavController().navigate(action)
        }, 3000)
    }
}
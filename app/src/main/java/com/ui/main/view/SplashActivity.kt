package com.ui.main.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.ioscameraandroidapp.R

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        mMoveToNextScreen()
    }

    private fun mMoveToNextScreen()
    {
//        Handler().postDelayed(Runnable {
//           startActivity(Intent(this, MainActivity::class.java))
//            finish()
           startActivity(Intent(this, CameraActivity::class.java))
            finish()
//        }, 3000)
    }
}
package com.usehover.hovertest.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.usehover.hovertest.R
import com.usehover.hovertest.home.HomeActivity

class SplashScreenActivity : AppCompatActivity() {

    private val splashTimeout = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({

            startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))

            finish()
        }, splashTimeout.toLong())

    }

}

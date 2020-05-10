package com.usehover.hovertest.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.usehover.hovertest.R
import com.usehover.hovertest.home.HomeActivity
import com.usehover.hovertest.store.PrefManager

class SplashScreen : Activity() {

    private val splashTimeout = 2000

    private lateinit var prefManager: PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)


        prefManager = PrefManager(this)


        Handler().postDelayed({
            prefManager = PrefManager(applicationContext)

            if (prefManager.isFirstTimeLaunch) {
                //walkthrough
                startActivity(Intent(this@SplashScreen, HomeActivity::class.java))
                Log.d("okh", "first time")
            } else {
                startActivity(Intent(this@SplashScreen, HomeActivity::class.java))
            }

            // close this activity
            finish()
        }, splashTimeout.toLong())

    }

}

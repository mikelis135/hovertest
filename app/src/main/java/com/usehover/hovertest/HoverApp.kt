package com.usehover.hovertest

import android.app.Application
import com.usehover.hovertest.di.component.AppComponent
import com.usehover.hovertest.di.component.DaggerAppComponent

class HoverApp : Application() {

    lateinit var appComponent: AppComponent


    override fun onCreate() {

        appComponent = DaggerAppComponent
                .builder()
                .application(this)
                .build()

        super.onCreate()
    }

    fun appComponent(): AppComponent {
        return appComponent
    }
}
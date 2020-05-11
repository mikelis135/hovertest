package com.usehover.hovertest.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.usehover.hovertest.store.PrefModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CacheModule {

    @Singleton
    @Provides
    fun providesAppCache(context: Context): PrefModel {
        val sharedPreferences = makeSharedPreferences(context)
        return PrefModel(sharedPreferences, sharedPreferences.edit())
    }

    private fun makeSharedPreferences(context: Context): SharedPreferences {

        return context.getSharedPreferences(
                "hover", Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun providesContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun providesGson(): Gson {
        return Gson()
    }
}
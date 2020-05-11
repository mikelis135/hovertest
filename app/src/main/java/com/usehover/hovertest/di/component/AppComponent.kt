package com.usehover.hovertest.di.component

import android.app.Application
import com.usehover.hovertest.HoverApp
import com.usehover.hovertest.di.module.CacheModule
import com.usehover.hovertest.di.module.PresentationModule
import com.usehover.hovertest.home.HomeActivity
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.store.PrefManager
import com.usehover.hovertest.transaction.NewTransactionActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
        modules = [PresentationModule::class,
            CacheModule::class]
)
interface AppComponent {

    fun sharedPrefs(): PrefManager

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: HoverApp)
    fun inject(activity: HomeActivity)
    fun inject(activity: ProfileActivity)
    fun inject(activity: NewTransactionActivity)

}
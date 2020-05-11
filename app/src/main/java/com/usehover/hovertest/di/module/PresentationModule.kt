package com.usehover.hovertest.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.usehover.hovertest.di.ViewModelFactory
import com.usehover.hovertest.home.HomeViewModel
import com.usehover.hovertest.profile.ProfileViewModel
import com.usehover.hovertest.transaction.NewTransactionViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Module
abstract class PresentationModule {

    @Binds
    @IntoMap
    @ViewModelKey(NewTransactionViewModel::class)
    abstract fun bindNewTransactionViewModel(
            viewModel: NewTransactionViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(
            viewModel: HomeViewModel
    ): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(
            viewModel: ProfileViewModel
    ): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@MustBeDocumented
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
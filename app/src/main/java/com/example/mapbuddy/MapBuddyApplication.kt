package com.example.mapbuddy

import android.app.Application
import com.example.mapbuddy.di.repositoryModule
import com.example.mapbuddy.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MapBuddyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@MapBuddyApplication)
            modules(
                repositoryModule,
                viewModelModule
            )
        }
    }

}
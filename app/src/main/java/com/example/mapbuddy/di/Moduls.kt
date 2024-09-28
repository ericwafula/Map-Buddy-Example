package com.example.mapbuddy.di

import com.example.mapbuddy.data.DefaultLocationRepository
import com.example.mapbuddy.domain.LocationRepository
import com.example.mapbuddy.presentation.DisplayViewModel
import com.example.mapbuddy.presentation.MainViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::DefaultLocationRepository).bind<LocationRepository>()
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::DisplayViewModel)
}
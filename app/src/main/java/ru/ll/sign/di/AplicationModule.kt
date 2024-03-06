package com.example.gameapplication.ui.di

import android.content.Context
import org.koin.dsl.module


fun applicationModule(context: Context) = module {
    single { context }
}
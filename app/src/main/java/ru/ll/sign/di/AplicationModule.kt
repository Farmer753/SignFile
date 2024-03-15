package ru.ll.sign.di

import android.content.Context
import org.koin.dsl.module

fun applicationModule(context: Context) = module {
    single { context }
}
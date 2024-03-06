package ru.ll.sign

import android.app.Application;
import com.example.gameapplication.ui.di.applicationModule
import com.example.gameapplication.ui.di.domainModule
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class SignFileApplicationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        // start Koin!
        startKoin {
            // declare modules
            modules(applicationModule(this@SignFileApplicationApp), domainModule())
        }
        Timber.d("SignFileApplicationApp")
    }
}

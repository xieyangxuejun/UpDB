package com.foretree.updb

import android.app.Application
import com.facebook.stetho.Stetho

/**
 * Created by silen on 13/04/2018.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        DatabaseManager.initDB(this)
    }
}
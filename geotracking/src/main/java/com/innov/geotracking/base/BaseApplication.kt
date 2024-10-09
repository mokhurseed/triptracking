package com.innov.geotracking.base

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import com.innov.geotracking.Constant
import com.innov.geotracking.di.AppModule
import com.innov.geotracking.di.NetworkModule
import com.innov.geotracking.utils.AppUtils
import com.innov.geotracking.utils.ImageUtils
import com.innov.geotracking.utils.PreferenceUtils
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class BaseApplication : Application() {

    companion object {
        lateinit var mContext: Context
        private const val TAG = "DatabaseConfig"
        private const val DATABASE_NAME = "geotracking.realm"
        lateinit var preferenceUtils: PreferenceUtils
    }

    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
        MultiDex.install(this)
        preferenceUtils = PreferenceUtils(mContext)
        initSingleton()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(listOf(AppModule, NetworkModule))
        }
        configureRealmDatabase()

        // Initialize the Branch object
    }

    fun getInstance(): BaseApplication {
        return this
    }

    private fun initSingleton() {
        AppUtils.setInstance()
        AppUtils.INSTANCE?.preferenceUtils = PreferenceUtils(this)
        ImageUtils.setImageInstance()
    }

    fun getAppContext(): Context {
        return mContext
    }

    private fun configureRealmDatabase() {
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .name(Constant.DATABASE_NAME)
            .schemaVersion(1)
            .allowWritesOnUiThread(true)
            .allowQueriesOnUiThread(true)
            .migration { realm, oldVersion, newVersion ->
                Log.e(
                    "TAG",
                    "Realm Version ${realm.version} Migration From $oldVersion To $newVersion"
                )
            }

            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }

}
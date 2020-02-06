package com.smttcn.safebox

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.smttcn.commons.helpers.BaseConfig
import com.smttcn.commons.helpers.PREFS_KEY
import com.smttcn.safebox.database.AppDatabase

// Todo: Try to do without the sqlite database and instead to put the encryption info into the
//      file header to more robust data storing

class MyApplication : Application(), Application.ActivityLifecycleCallbacks{

    companion object {
        var globalAppAuthenticated: String = "no"
        lateinit var mainActivityContext: Context
        private var instance: MyApplication? = null
        private var baseConfig: BaseConfig? = null

        fun getMainContext(): Context {
            return mainActivityContext
        }

        fun setMainContext(context: Context): Unit {
            mainActivityContext = context

        }

        fun getAppContext() : Context = instance!!.applicationContext

        fun getBaseConfig() : BaseConfig {
            if (baseConfig != null)
                return baseConfig!!
            else {
                if (instance != null) {
                    val prefs = instance!!.applicationContext.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                    baseConfig = BaseConfig.newInstance(prefs)
                }
                return baseConfig!!
            }
        }
    }

    var activityReferences: Int = 0
    var isActivityChangingConfigurations: Boolean = false

    init {
        instance = this
    }

    fun lockApp() {
        globalAppAuthenticated = "no"
        AppDatabase.close()
    }

    override fun onCreate() {
        super.onCreate()
        activityReferences = 0
        isActivityChangingConfigurations = false

        registerActivityLifecycleCallbacks(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { }

    override fun onActivityStarted(activity: Activity) {
        // Tracing activity status to determine if the app still in the foreground
        //      in order to set the Authentication status and trigger the login screen
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            lockApp()
        }
    }

    override fun onActivityResumed(activity: Activity) { }

    override fun onActivityPaused(activity: Activity) { }

    override fun onActivityStopped(activity: Activity) {
        // Tracing activity status to determine if the app still in the foreground
        //      in order to set the Authentication status and trigger the login screen
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            lockApp()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

    override fun onActivityDestroyed(activity: Activity) { }
}
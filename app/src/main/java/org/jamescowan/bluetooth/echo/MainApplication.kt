package org.jamescowan.bluetooth.echo

import android.app.Application
import android.os.Parcel
import android.os.Parcelable
import timber.log.Timber

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            //Timber.plant(CrashlyticsTree())
        }

        //Fabric.with(this, Crashlytics());

        Timber.i("Application started")
    }


}
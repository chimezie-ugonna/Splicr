package com.splicr.app

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.jakewharton.threetenabp.AndroidThreeTen
import com.splicr.app.utils.SharedPreferenceUtil

class Splicr : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferenceUtil.init(this)
        AndroidThreeTen.init(this)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        val config = HashMap<String, Any>()
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME)
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY)
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET)
        config.put("secure", true)
        MediaManager.init(this, config)
    }
}
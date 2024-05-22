package com.splicr.app

import android.app.Application
import com.splicr.app.utils.SharedPreferenceUtil

class Splicr : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreferenceUtil.init(this)
    }
}
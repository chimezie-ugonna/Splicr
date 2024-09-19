package com.splicr.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.splicr.app.R

object SharedPreferenceUtil {
    private lateinit var sp: SharedPreferences
    private lateinit var spe: SharedPreferences.Editor

    fun init(context: Context) {
        sp = context.getSharedPreferences(
            context.resources.getString(R.string.app_name), Context.MODE_PRIVATE
        )
        spe = sp.edit()
    }

    fun onboarded(data: Boolean) {
        spe.putBoolean("onboarded", data)
        spe.commit()
    }

    fun onboarded(): Boolean {
        return sp.getBoolean("onboarded", false)
    }

    fun atHomeScreen(data: Boolean) {
        spe.putBoolean("atHomeScreen", data)
        spe.commit()
    }

    fun atHomeScreen(): Boolean {
        return sp.getBoolean("atHomeScreen", false)
    }
}
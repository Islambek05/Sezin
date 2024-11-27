package com.example.sezin.user

import android.content.Context
import android.content.SharedPreferences

object AuthUtils {
    private const val PREFS_NAME = "user_prefs"
    private const val LOGGED_IN_KEY = "is_logged_in"

    fun setLoggedIn(context: Context) {
        getSharedPreferences(context).edit()
            .putBoolean(LOGGED_IN_KEY, true)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(LOGGED_IN_KEY, false)
    }

    fun logout(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}


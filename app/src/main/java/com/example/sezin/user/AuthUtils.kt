package com.example.sezin.user

import android.content.Context

object AuthUtils {
    private const val PREF_NAME = "auth_preferences"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    fun isLoggedIn(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            apply()
        }
    }
}

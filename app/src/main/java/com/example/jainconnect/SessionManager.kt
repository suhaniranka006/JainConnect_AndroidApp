package com.example.jainconnect

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    // 1. Define constants for your SharedPreferences
    companion object {
        private const val PREF_NAME = "MyAppSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    // 2. Initialize SharedPreferences
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    /**
     * Call this function when the user successfully logs in or out
     */
    fun saveLoginStatus(isLoggedIn: Boolean) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.apply() // .apply() saves the changes in the background
    }

    /**
     * Call this function to check if the user is logged in
     */
    fun isLoggedIn(): Boolean {
        // The 'false' is the default value if the key doesn't exist
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Optional: Call this on logout to clear all session data
     */
    fun clearSession() {
        editor.clear()
        editor.apply()
        // After clearing, you might want to ensure the logged-in flag is false
        saveLoginStatus(false)
    }
}
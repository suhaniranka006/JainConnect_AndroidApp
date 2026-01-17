package com.mycompany.jainconnect.utils

object ValidationUtils {

    // A simple Regex for email validation (Standard pattern)
    // We use this instead of android.util.Patterns so we can Unit Test it without an Emulator!
    private val EMAIL_PATTERN = Regex(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_PATTERN.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        // Example: Password must be at least 6 characters
        return password.length >= 6
    }
}

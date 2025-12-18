package com.mycompany.jainconnect.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AgeUtils {
    fun calculateAge(dobString: String?): String {
        if (dobString.isNullOrEmpty()) return "N/A"
        
        // If it's already a simple number (1-3 digits), return it as age
        if (dobString.matches(Regex("\\d{1,3}"))) {
            return dobString
        }

        return try {
            val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val sdfSimple = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            // Try ISO format first
            val date = try {
                 sdfIso.parse(dobString)
            } catch (e: Exception) {
                 try {
                     sdfSimple.parse(dobString)
                 } catch (e2: Exception) {
                     null
                 }
            }

            if (date != null) {
                val dob = Calendar.getInstance()
                dob.time = date
                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age.toString()
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }
}

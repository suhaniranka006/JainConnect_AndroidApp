package com.mycompany.jainconnect.ui.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mycompany.jainconnect.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    // Launches LoginActivity before each test
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLoginUIElementsVisible() {
        // Check if Email field is displayed
        onView(withId(R.id.etLoginEmail))
            .check(matches(isDisplayed()))

        // Check if Password field is displayed
        onView(withId(R.id.etLoginPassword))
            .check(matches(isDisplayed()))

        // Check if Login Button is displayed
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))

        // Check if "New Here? Go to Sign Up" text is visible
        onView(withId(R.id.tvGoToSignUp))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testInvalidEmailValidationError() {
        // 1. Type invalid email
        onView(withId(R.id.etLoginEmail))
            .perform(typeText("invalid-email"), closeSoftKeyboard())

        // 2. Click Login button
        onView(withId(R.id.btnLogin))
            .perform(click())

        // 3. Verify error message on EditText
        onView(withId(R.id.etLoginEmail))
            .check(matches(hasErrorText("Please enter a valid email")))
    }

    @Test
    fun testEmptyPasswordValidationError() {
        // 1. Type valid email
        onView(withId(R.id.etLoginEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        // 2. Leave password empty and click Login
        onView(withId(R.id.btnLogin))
            .perform(click())

        // 3. Verify error message on Password field
        onView(withId(R.id.etLoginPassword))
            .check(matches(hasErrorText("Password cannot be empty")))
    }
}

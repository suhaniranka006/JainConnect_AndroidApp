package com.mycompany.jainconnect.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isValidEmail returns true for valid email`() {
        val result = ValidationUtils.isValidEmail("test@example.com")
        assertTrue(result)
    }

    @Test
    fun `isValidEmail returns false for empty email`() {
        val result = ValidationUtils.isValidEmail("")
        assertFalse(result)
    }

    @Test
    fun `isValidEmail returns false for email without at symbol`() {
        val result = ValidationUtils.isValidEmail("testexample.com")
        assertFalse(result)
    }

    @Test
    fun `isValidPassword returns true for 6 chars`() {
        // Boundary condition
        val result = ValidationUtils.isValidPassword("123456")
        assertTrue(result)
    }

    @Test
    fun `isValidPassword returns false for less than 6 chars`() {
        val result = ValidationUtils.isValidPassword("12345")
        assertFalse(result)
    }
}

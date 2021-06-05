package com.example.MobSecApp

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class InternetHelperTest {
    lateinit var internetHelper: InternetHelper

    @Before
    fun init() {
        internetHelper = InternetHelper("https://httpbin.org/anything")
    }

    @Test
    fun sendMessagePost() {
        internetHelper.sendMessagePost("12345", "testtest".toByteArray())
    }

    @Test
    fun base64Encode() {
    }

    @Test
    fun base64Decode() {
    }
}

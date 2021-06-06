package com.example.MobSecApp

import android.util.Log
import okhttp3.*
import okio.IOException
import java.util.*


class InternetHelper(private val url: String) {

    fun sendMessagePost(phoneNumber: String, cipherText: ByteArray): Int {
        val client = OkHttpClient()
        val cipherAsBase64 = base64Encode(cipherText)
        val body = FormBody.Builder()
            .add("userphone", phoneNumber)
            .add("message", cipherAsBase64)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val call: Call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e("ben", "Sending failed!")
                throw okio.IOException("post failed $e")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    for ((name, value) in response.headers) {
                        Log.i("ben", "$name: $value")
                    }

                    Log.i("ben", response.code.toString())
                    Log.i("ben", response.body!!.string())
                    Log.i("ben", "Post sent successfully!")
                }
            }
        })

        return 0
    }

    fun base64Encode(data: ByteArray): String {
        return String(Base64.getEncoder().encode(data))
    }

    fun base64Decode(data: String): ByteArray {
        return Base64.getDecoder().decode(data)
    }
}

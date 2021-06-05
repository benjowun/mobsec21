package com.example.MobSecApp

import android.util.Log
import at.favre.lib.crypto.HKDF
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class CryptoStuff {
    private val sr = SecureRandom()
    private var sk = ByteArray(0) // TODO: how to securely store

    fun generateSecretKey(): ByteArray {
        val key = ByteArray(16)
        sr.nextBytes(key)
        this.sk = key
        return key
    }

    fun setSK(key: ByteArray) {
        sk = key
    }

    fun isSKNull(): Boolean {
        return sk.isEmpty()
    }

    fun updateSK(): ByteArray {
        val newKey = HKDF.fromHmacSha256().extractAndExpand(ByteArray(0), this.sk, null, 16)

        this.sk = newKey
        return newKey
    }

    // can add associated data to hmac if we need for the server somehow
    fun encryptSecure(pt: ByteArray): ByteArray {
        val iv = ByteArray(16)
        sr.nextBytes(iv) // get unique IV

        // generate enc and auth keypair, using HKDF
        val encKey: ByteArray = HKDF.fromHmacSha256().expand(
            this.sk, "encKey".toByteArray(
                StandardCharsets.UTF_8
            ), 16
        )
        val authKey: ByteArray = HKDF.fromHmacSha256().expand(
            this.sk, "authKey".toByteArray(
                StandardCharsets.UTF_8
            ), 32
        )

        // ENC
        val cipher: Cipher =
            Cipher.getInstance("AES/CBC/PKCS5Padding") //actually  PKCS7, we encrypt then MAC, so padding is ok
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encKey, "AES"), IvParameterSpec(iv))
        val ciphertext: ByteArray = cipher.doFinal(pt)

        // MAC
        val macKey: SecretKey = SecretKeySpec(authKey, "HmacSHA256")
        val hmac: Mac = Mac.getInstance("HmacSHA256")
        hmac.init(macKey)
        hmac.update(iv)
        hmac.update(ciphertext)
        // could add associated data here
        val mac: ByteArray = hmac.doFinal()
        // Construction: IV size, IV, MAC size, MAC, ciphertext
        val buffer: ByteBuffer = ByteBuffer.allocate(1 + iv.size + 1 + mac.size + ciphertext.size)
        buffer.put(iv.size.toByte())
        buffer.put(iv)
        buffer.put(mac.size.toByte())
        buffer.put(mac)
        buffer.put(ciphertext)
        val fullMessage: ByteArray = buffer.array()

        // clear memory as good as possible
        authKey.map { it * 0 }
        encKey.map { it * 0 }

        // update sk
        updateSK()

        return fullMessage
    }

    fun decryptSecure(fullMessage: ByteArray): ByteArray? {
        val buffer = ByteBuffer.wrap(fullMessage)
        // read IV
        val lengthIV = buffer.get().toInt()
        if (lengthIV != 16) { // length of IV must match protocol format
            Log.w("CRYPTO", "Invalid IV length: $lengthIV")
            return null
        }
        val iv = ByteArray(lengthIV)
        buffer.get(iv)

        // read MAC
        val lengthMAC = buffer.get().toInt()
        if (lengthMAC != 32) { // MAC must be 32B
            Log.w("CRYPTO", "Invalid MAC length: $lengthMAC")
            return null
        }
        val encMac = ByteArray(lengthMAC)
        buffer.get(encMac)

        // read ciphertext
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)

        // generate the sub keys from secret key
        val encKey = HKDF.fromHmacSha256()
            .expand(this.sk, "encKey".toByteArray(StandardCharsets.UTF_8), 16)
        val authKey = HKDF.fromHmacSha256()
            .expand(this.sk, "authKey".toByteArray(StandardCharsets.UTF_8), 32)
        val macKey: SecretKey = SecretKeySpec(authKey, "HmacSHA256")
        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(macKey)
        hmac.update(iv)
        hmac.update(ciphertext)

        // if associated data, cover here
        val mac = hmac.doFinal()
        // Verify Authentication
        if (!MessageDigest.isEqual(mac, encMac)) {
            Log.w("CRYPTO", "Message could not be Authenticated!")
            return null
        }

        // Decryption
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(encKey, "AES"),
            IvParameterSpec(iv)
        )

        // update SK
        updateSK()

        return cipher.doFinal(ciphertext)

    }
}

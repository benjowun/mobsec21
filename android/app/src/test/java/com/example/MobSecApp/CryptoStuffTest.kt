package com.example.MobSecApp

import at.favre.lib.crypto.HKDF
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.nio.charset.StandardCharsets

class CryptoStuffTest {
    val cs: CryptoStuff = CryptoStuff()

    @Test
    fun generateSecretKey() {
        val key1 = cs.generateSecretKey()
        val key2 = cs.generateSecretKey()
        assert(!key1.contentEquals(key2))
    }

    @Test
    fun encryptSecure() {
        val sk = cs.generateSecretKey()
        val pt = "thisisatesttextthatis longer with spaces and 1223".toByteArray()
        val enc = cs.encryptSecure(pt)
        println(enc)
    }

    @Test
    fun decryptSecure() {
        val sk = cs.generateSecretKey()
        val pt = "thisisatesttextthatis longer with spaces and 1223".toByteArray()
        val pt2 = "thisisatesttext".toByteArray()
        val pt3 = "this is a test text".toByteArray()
        val enc = cs.encryptSecure(pt)
        val enc2 = cs.encryptSecure(pt2)
        val enc3 = cs.encryptSecure(pt3)

        cs.setSK(sk)
        var dec = cs.decryptSecure(enc)
        assert(dec != null)
        println(String(dec!!))

        dec = cs.decryptSecure(enc2)
        assert(dec != null)
        println(String(dec!!))

        dec = cs.decryptSecure(enc3)
        assert(dec != null)
        println(String(dec!!))
    }

    @Test
    fun testGen() {
        val sk = cs.generateSecretKey()
        val cs2 = CryptoStuff()
        cs2.setSK(sk)

        val key1 = cs.updateSK()
        val key2 = cs.updateSK()

        val key21 = cs2.updateSK()
        val key22 = cs2.updateSK()

        assert(key1.contentEquals(key21))
        assert(key2.contentEquals(key22))

        assert(!key1.contentEquals(key2))
        assert(!key21.contentEquals(key22))
    }

    // demonstrates full behaviour
    @Test
    fun fullTest() {
        // message to encrypt
        val pt1 = "thisisatesttextthatis longer with spaces and 1223".toByteArray()
        val pt2 = "thisisatesttext".toByteArray()
        val pt3 = "this is a test text".toByteArray()

        // other device, cs is phone
        val cs2 = CryptoStuff()
        // generate key on other device, share via qr code
        val sk2 = cs2.generateSecretKey()
        val sk1 = sk2.clone()
        cs.setSK(sk1)

        assert(sk2.contentEquals(sk1))

        // on phone encryption
        val enc1 = cs.encryptSecure(pt1)
        val enc2 = cs.encryptSecure(pt2)
        val enc3 = cs.encryptSecure(pt3)

        // decryption on pc
        val dec1 = cs2.decryptSecure(enc1)
        val dec2 = cs2.decryptSecure(enc2)
        val dec3 = cs2.decryptSecure(enc3)

        assert(dec1.contentEquals(pt1))
        assert(dec2.contentEquals(pt2))
        assert(dec3.contentEquals(pt3))
//        println(String(dec1!!))
//        println(String(dec2!!))
//        println(String(dec3!!))
    }
}
package com.example.MobSecApp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.*

class MainActivity : AppCompatActivity() {
    private val RC_PERMISSION: Int = 10

    // for testing, TODO: not sure if it is secure to have the url in plaintext... but I guess it is fine since everything is encrypted
    private val apiAddress = "https://httpbin.org/anything"

    private lateinit var sendButton: Button
    private lateinit var codeScanner: CodeScanner //https://github.com/yuriy-budiyev/code-scanner
    private var permissionsGranted = false
    private val cryptoStuff = CryptoStuff()
    private val internetHelper = InternetHelper(apiAddress)

    var phoneNumber: String = ""
    var message: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendButton = findViewById((R.id.button_send))
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        val textField = findViewById<EditText>(R.id.editTextTextMessage)
        val textFieldPhone = findViewById<EditText>(R.id.editTextPhone)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                var skCandidate = it.rawBytes
                Toast.makeText(
                    this,
                    "Scan result: ${it.text}:   ${skCandidate.size}",
                    Toast.LENGTH_LONG
                ).show()
                if (skCandidate.size >= 16) { // TODO: this allows people with physical access to set null keys... but if they have physical access I guess youre screwed anyways
                    if (skCandidate.size > 16)
                        skCandidate = skCandidate.sliceArray(0..16)
                    cryptoStuff.setSK(skCandidate)
                    sendButton.isEnabled = true
                    textField.isEnabled = true
                    textFieldPhone.isEnabled = true
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(
                    this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false
            requestPermissions(arrayOf(Manifest.permission.CAMERA), RC_PERMISSION)
        } else {
            permissionsGranted = true
            codeScanner.startPreview()
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true
                codeScanner.startPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionsGranted)
            codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    fun sendMessage(view: View) {
        val text = findViewById<EditText>(R.id.editTextTextMessage)
        val number = findViewById<EditText>(R.id.editTextPhone)
        message = text.text.toString()
        phoneNumber = number.text.toString()
        val toast = Toast.makeText(applicationContext, "$message $phoneNumber", Toast.LENGTH_SHORT)
        toast.show()

        // to ensure that we have set a valid SK
        assert(!cryptoStuff.isSKNull()) // fails since no QR code was read
        val messageBytes = message.toByteArray()
        val messageEncrypted = cryptoStuff.encryptSecure(messageBytes)

        // TODO: send to server here
        internetHelper.sendMessagePost(phoneNumber, messageEncrypted)
    }
}

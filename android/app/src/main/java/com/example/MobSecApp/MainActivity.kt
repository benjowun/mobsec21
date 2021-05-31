package com.example.MobSecApp

import android.Manifest
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
    private lateinit var sendButton: Button
    private lateinit var codeScanner: CodeScanner //https://github.com/yuriy-budiyev/code-scanner
    private var permissionsGranted = false
    private val cryptoStuff = CryptoStuff()

    var message: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendButton = findViewById((R.id.button_send))
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        val textField = findViewById<EditText>(R.id.editTextTextMessage)

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
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                val skCandidate = it.rawBytes
                if (skCandidate.size == 16) { // TODO: this allows people with physical access to set null keys... but if they have physical access I guess youre screwed anyways
                    cryptoStuff.setSK(it.rawBytes)
                    sendButton.isEnabled = true
                    textField.isEnabled = true
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false;
            requestPermissions(arrayOf(Manifest.permission.CAMERA), RC_PERMISSION);
        } else {
            permissionsGranted = true;
            codeScanner.startPreview()
        }
    }

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
        message = text.text.toString()
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()

        // to ensure that we have set a valid SK
        assert(!cryptoStuff.isSKNull()) // fails since no QR code was read
        val messageBytes = message.toByteArray()
        val messageEncrypted = cryptoStuff.encryptSecure(messageBytes)


        // TODO: send to server here
    }
}
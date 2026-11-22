package com.you.headdot

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, CaptureService::class.java).apply {
                action = "START"
                putExtra("code", result.resultCode)
                putExtra("data", result.data)
            }
            startForegroundService(intent)
            status.text = "Capture: ON"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply { text = "" }

        val start = Button(this).apply {
            text = "1. Start Capture"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    askCapture()
                }
            }
        }

        val acc = Button(this).apply {
            text = "2. Accessibility ON"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val aim = Button(this).apply {
            text = "3. Auto Aim ON/OFF"
            setOnClickListener {
                AimService.enabled = !AimService.enabled
                status.text = if (AimService.enabled) "Auto Aim: ON" else "Auto Aim: OFF"
            }
        }

        val stop = Button(this).apply {
            text = "Stop"
            setOnClickListener {
                val intent = Intent(this@MainActivity, CaptureService::class.java).apply {
                    action = "STOP"
                }
                startService(intent)
            }
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(start)
            addView(acc)
            addView(aim)
            addView(stop)
            addView(status)
        }

        setContentView(ScrollView(this).apply { addView(box) })
    }

    private fun askCapture() {
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        captureLauncher.launch(mpm.createScreenCaptureIntent())
    }
}

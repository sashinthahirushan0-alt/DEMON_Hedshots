package com.you.headdot

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply { text = "" }

        val start = Button(this).apply {
            text = "1. Start (capture)"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ))
                } else askCapture()
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
                startService(Intent(this@MainActivity, CaptureService::class.java).apply { action = "STOP" })
            }
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(start); addView(acc); addView(aim); addView(stop); addView(status)
        }
        setContentView(ScrollView(this).apply { addView(box) })
    }

    private fun askCapture() {
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), 1)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            startForegroundService(Intent(this, CaptureService::class.java).apply {
                putExtra("code", resultCode)
                putExtra("data", data)
            })
            status.text = "Capture: ON"
        }
    }
}

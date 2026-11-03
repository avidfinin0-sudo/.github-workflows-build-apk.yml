package com.xiaomi.island

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(30), dp(22), dp(24))
            setBackgroundColor(0xFF08090D.toInt())
        }

        fun label(value: String, size: Float, color: Int = 0xFFFFFFFF.toInt()) =
            TextView(this).apply {
                text = value
                textSize = size
                setTextColor(color)
            }

        content.addView(label("Xiaomi Island", 28f))
        content.addView(label("Una isla flotante inspirada en HyperOS", 14f, 0xFF9DA3B4.toInt()))

        val preview = TextView(this).apply {
            text = "●     XIAOMI ISLAND\n\nMúsica  ·  Avisos  ·  Batería"
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            setBackgroundColor(0xFF191C25.toInt())
        }
        content.addView(preview, LinearLayout.LayoutParams(-1, dp(112)).apply {
            topMargin = dp(26)
            bottomMargin = dp(20)
        })

        content.addView(label("CONFIGURACIÓN", 12f, 0xFF8AA4FF.toInt()))

        content.addView(actionButton("1. Permitir mostrar sobre otras aplicaciones") {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            } else {
                Toast.makeText(this, "El permiso ya está activado", Toast.LENGTH_SHORT).show()
            }
        })

        content.addView(actionButton("2. Activar acceso a notificaciones (opcional)") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })

        content.addView(actionButton("3. Iniciar Xiaomi Island") {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Primero activa el permiso de superposición", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val serviceIntent = Intent(this, IslandService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    Toast.makeText(this, "Xiaomi Island iniciada", Toast.LENGTH_SHORT).show()
                } catch (error: Exception) {
                    Toast.makeText(this, "No se pudo iniciar: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        content.addView(actionButton("Detener Xiaomi Island") {
            stopService(Intent(this, IslandService::class.java))
            Toast.makeText(this, "Xiaomi Island detenida", Toast.LENGTH_SHORT).show()
        })

        content.addView(label(
            "\nUso: concede el permiso de superposición y pulsa Iniciar. Toca la isla para expandirla o contraerla. El acceso a notificaciones es opcional.",
            13f,
            0xFF9DA3B4.toInt()
        ))

        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun actionButton(title: String, action: () -> Unit): Button =
        Button(this).apply {
            text = title
            isAllCaps = false
            setOnClickListener { action() }
        }
}

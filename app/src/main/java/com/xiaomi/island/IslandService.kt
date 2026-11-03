package com.xiaomi.island

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.animation.ValueAnimator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IslandService : Service() {

    companion object {
        const val ACTION_NOTIFICATION = "com.xiaomi.island.NEW_NOTIFICATION"
        private const val CHANNEL_ID = "xiaomi_island_service"
        private const val NOTIFICATION_ID = 42
    }

    private lateinit var windowManager: WindowManager
    private var islandView: LinearLayout? = null
    private var expanded = false
    private var headline = "Xiaomi Island"
    private var message = "Toca para ver información"
    private var batteryText = "Batería"
    private val handler = Handler(Looper.getMainLooper())
    private var notificationReceiver: BroadcastReceiver? = null
    private var batteryReceiver: BroadcastReceiver? = null

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildOngoingNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        if (Settings.canDrawOverlays(this)) {
            showIsland()
        }

        registerAppNotificationReceiver()
        registerBatteryReceiver()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Xiaomi Island",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildOngoingNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("Xiaomi Island está activa")
            .setContentText("La isla flotante está funcionando")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun registerAppNotificationReceiver() {
        val filter = IntentFilter(ACTION_NOTIFICATION)
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                headline = intent?.getStringExtra("title") ?: "Notificación"
                message = intent?.getStringExtra("body") ?: "Nuevo aviso"
                if (!expanded) expandIsland()
                renderIsland()
                handler.postDelayed({
                    if (expanded) collapseIsland()
                }, 5000L)
            }
        }
        val receiver = notificationReceiver ?: return
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra("level", -1) ?: -1
                val plugged = (intent?.getIntExtra("plugged", 0) ?: 0) != 0
                batteryText = if (plugged) "⚡ Cargando · $level%" else "Batería · $level%"
                if (expanded) renderIsland()
            }
        }
        val receiver = batteryReceiver ?: return
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }
    }

    private fun showIsland() {
        if (islandView != null || !Settings.canDrawOverlays(this)) return

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(7), dp(14), dp(7))
            background = GradientDrawable().apply {
                setColor(Color.rgb(8, 9, 13))
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.argb(55, 255, 255, 255))
            }
            elevation = dp(12).toFloat()
        }
        islandView = root
        renderIsland()

        val params = WindowManager.LayoutParams(
            dp(148),
            dp(38),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(5)
        }

        root.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - downX) < dp(18) &&
                            kotlin.math.abs(event.rawY - downY) < dp(18)
                        ) {
                            toggleIsland()
                        }
                        return true
                    }
                }
                return true
            }
        })

        try {
            windowManager.addView(root, params)
            root.alpha = 0f
            root.animate()
                .alpha(1f)
                .setDuration(280)
                .setInterpolator(DecelerateInterpolator())
                .start()
        } catch (_: Exception) {
            islandView = null
        }
    }

    private fun renderIsland() {
        val root = islandView ?: return
        root.removeAllViews()

        if (!expanded) {
            val compact = TextView(this).apply {
                text = "●"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(0xFFB8C7FF.toInt())
            }
            root.addView(compact, LinearLayout.LayoutParams(-1, -1))
        } else {
            val title = TextView(this).apply {
                text = headline
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                maxLines = 1
            }
            val body = TextView(this).apply {
                text = message
                textSize = 12f
                setTextColor(0xFFD4D8E2.toInt())
                maxLines = 2
            }
            val status = TextView(this).apply {
                text = "$batteryText  ·  ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}"
                textSize = 11f
                setTextColor(0xFF9DA3B4.toInt())
            }
            root.addView(title)
            root.addView(body)
            root.addView(status)
        }
    }

    private fun toggleIsland() {
        if (expanded) collapseIsland() else expandIsland()
    }

    private fun expandIsland() {
        if (expanded) return
        expanded = true
        animateIslandSize(dp(148), dp(38), dp(292), dp(112))
        renderIsland()
    }

    private fun collapseIsland() {
        if (!expanded) return
        expanded = false
        animateIslandSize(dp(292), dp(112), dp(148), dp(38))
        renderIsland()
    }

    private fun animateIslandSize(startWidth: Int, startHeight: Int, endWidth: Int, endHeight: Int) {
        val root = islandView ?: return
        val params = root.layoutParams as? WindowManager.LayoutParams ?: return
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 240L
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                params.width = (startWidth + (endWidth - startWidth) * fraction).toInt()
                params.height = (startHeight + (endHeight - startHeight) * fraction).toInt()
                try {
                    windowManager.updateViewLayout(root, params)
                } catch (_: Exception) {
                    cancel()
                }
            }
            start()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (islandView == null && Settings.canDrawOverlays(this)) {
            showIsland()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        notificationReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) { }
        }
        batteryReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) { }
        }
        islandView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) { }
        }
        islandView = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

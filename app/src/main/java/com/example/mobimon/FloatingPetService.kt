package com.example.mobimon

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class FloatingPetService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: TriangleOverlayView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val handler = Handler(Looper.getMainLooper())
    private var currentX = 0f
    private var currentY = 0f
    private var targetX = 0f
    private var targetY = 0f
    private var elapsedMs = 0L

    private val moveRunnable = object : Runnable {
        override fun run() {
            step()
            handler.postDelayed(this, FRAME_DELAY_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val character = CharacterType.entries.find { it.name == intent?.getStringExtra(EXTRA_CHARACTER) }
            ?: CharacterType.BLUE_TRIANGLE

        startForeground(NOTIFICATION_ID, buildNotification())
        showOrUpdateOverlay(character)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(moveRunnable)
        overlayView?.let { runCatching { windowManager.removeView(it) } }
        overlayView = null
    }

    private fun showOrUpdateOverlay(character: CharacterType) {
        val existing = overlayView
        if (existing != null) {
            existing.color = character.color.toArgb()
            return
        }

        val density = resources.displayMetrics.density
        val sizePx = (VIEW_SIZE_DP * density).roundToInt()

        val view = TriangleOverlayView(this, character.color.toArgb())
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = ((resources.displayMetrics.widthPixels - sizePx) / 2f).roundToInt()
            y = ((resources.displayMetrics.heightPixels - sizePx) / 2f).roundToInt()
        }

        windowManager.addView(view, params)
        overlayView = view
        layoutParams = params
        currentX = params.x.toFloat()
        currentY = params.y.toFloat()
        pickNewTarget(sizePx)

        handler.removeCallbacks(moveRunnable)
        handler.post(moveRunnable)
    }

    private fun step() {
        val params = layoutParams ?: return
        val view = overlayView ?: return
        elapsedMs += FRAME_DELAY_MS

        val dx = targetX - currentX
        val dy = targetY - currentY
        val distance = kotlin.math.hypot(dx, dy)

        if (distance < SPEED_PX_PER_FRAME) {
            pickNewTarget(view.width.takeIf { it > 0 } ?: (VIEW_SIZE_DP * resources.displayMetrics.density).roundToInt())
        } else {
            currentX += dx / distance * SPEED_PX_PER_FRAME
            currentY += dy / distance * SPEED_PX_PER_FRAME
        }

        val bob = sin(elapsedMs / BOB_PERIOD_MS * (2 * Math.PI)) * BOB_AMPLITUDE_PX

        params.x = currentX.roundToInt()
        params.y = (currentY + bob).roundToInt()
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun pickNewTarget(sizePx: Int) {
        val maxX = (resources.displayMetrics.widthPixels - sizePx).coerceAtLeast(0)
        val maxY = (resources.displayMetrics.heightPixels - sizePx).coerceAtLeast(0)
        targetX = Random.nextInt(0, maxX + 1).toFloat()
        targetY = Random.nextInt(0, maxY + 1).toFloat()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "떠다니는 캐릭터",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, FloatingPetService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.presence_online)
            .setContentTitle("캐릭터가 화면 위를 돌아다니고 있어요")
            .setContentText("탭하면 멈출 수 있어요")
            .setOngoing(true)
            .addAction(0, "멈추기", stopPendingIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP = "com.example.mobimon.action.STOP"
        const val EXTRA_CHARACTER = "com.example.mobimon.extra.CHARACTER"
        private const val CHANNEL_ID = "floating_pet_channel"
        private const val NOTIFICATION_ID = 1001
        private const val VIEW_SIZE_DP = 72
        private const val FRAME_DELAY_MS = 16L
        private const val SPEED_PX_PER_FRAME = 4f
        private const val BOB_PERIOD_MS = 1500.0
        private const val BOB_AMPLITUDE_PX = 12.0
    }
}

package com.example.mobimon.feature.overlay

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mobimon.core.domain.pet.CharacterType

/**
 * feature-overlay 모듈의 유일한 공개 진입점. 다른 모듈(app)은 [FloatingPetService]나
 * 인텐트 extra 키 같은 내부 구현을 몰라도 이 객체만으로 오버레이를 제어할 수 있다.
 */
object OverlayPetController {

    fun hasOverlayPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                activity,
                "이 기기에는 오버레이 권한 설정 화면이 없어요. adb로 직접 허용해야 해요:\n" +
                    "adb shell appops set ${activity.packageName} SYSTEM_ALERT_WINDOW allow",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun start(context: Context, character: CharacterType) {
        val intent = Intent(context, FloatingPetService::class.java).apply {
            putExtra(FloatingPetService.EXTRA_CHARACTER, character.name)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        val intent = Intent(context, FloatingPetService::class.java).apply {
            action = FloatingPetService.ACTION_STOP
        }
        context.startService(intent)
    }
}

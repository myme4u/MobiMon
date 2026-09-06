package com.example.mobimon

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobimon.core.domain.pet.CharacterType
import com.example.mobimon.core.ui.component.MobiMonTopBar
import com.example.mobimon.core.ui.theme.MobiMonTheme
import com.example.mobimon.feature.overlay.OverlayPetController
import com.example.mobimon.feature.pet.PetRoutes
import com.example.mobimon.feature.pet.PetSelectionStore

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 알림 권한이 거부되어도 캐릭터는 계속 돌아다닐 수 있어 별도 처리하지 않음 */ }

    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
            { keepSplashOnScreen = false },
            SPLASH_DISPLAY_DURATION_MS
        )

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            var isRoaming by remember { mutableStateOf(false) }

            // 선택된 Pet이 없으면(최초 실행/재설치 직후) 홈을 건너뛰고 바로 선택 화면으로 진입한다.
            val startDestination = remember {
                if (PetSelectionStore.getSelectedCharacter(context) == null) {
                    PetRoutes.PET_SELECTION
                } else {
                    AppRoutes.HOME
                }
            }

            // backStackEntry를 읽어 구독해야 백스택이 바뀔 때 canGoBack이 다시 계산된다.
            val backStackEntry by navController.currentBackStackEntryAsState()
            val canGoBack = backStackEntry != null && navController.previousBackStackEntry != null

            MobiMonTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (canGoBack) {
                            MobiMonTopBar(onBack = { navController.popBackStack() })
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding),
                        isRoaming = isRoaming,
                        onReleaseCharacterRequested = { character ->
                            if (OverlayPetController.hasOverlayPermission(this)) {
                                isRoaming = true
                                releaseCharacter(character)
                            } else {
                                OverlayPetController.requestOverlayPermission(this)
                            }
                        },
                        onStopCharacterRequested = {
                            OverlayPetController.stop(this)
                            isRoaming = false
                        }
                    )
                }
            }
        }
    }

    private fun releaseCharacter(character: CharacterType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        PetSelectionStore.setSelectedCharacter(this, character)
        OverlayPetController.start(this, character)
        moveTaskToBack(true)
    }

    companion object {
        private const val SPLASH_DISPLAY_DURATION_MS = 2000L
    }
}

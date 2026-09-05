package com.example.mobimon

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mobimon.ui.theme.MobiMonTheme

enum class CharacterType(val label: String, val color: Color) {
    BLUE_TRIANGLE("파란색 삼각형", Color(0xFF2196F3)),
    RED_TRIANGLE("빨간색 삼각형", Color(0xFFF44336))
}

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
            MobiMonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRequestOverlayPermission = ::requestOverlayPermission,
                        onReleaseCharacter = ::releaseCharacter,
                        onStopCharacter = ::stopCharacter
                    )
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "이 기기에는 오버레이 권한 설정 화면이 없어요. adb로 직접 허용해야 해요:\n" +
                    "adb shell appops set $packageName SYSTEM_ALERT_WINDOW allow",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun releaseCharacter(character: CharacterType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val intent = Intent(this, FloatingPetService::class.java).apply {
            putExtra(FloatingPetService.EXTRA_CHARACTER, character.name)
        }
        ContextCompat.startForegroundService(this, intent)
        moveTaskToBack(true)
    }

    private fun stopCharacter() {
        val intent = Intent(this, FloatingPetService::class.java).apply {
            action = FloatingPetService.ACTION_STOP
        }
        startService(intent)
    }

    companion object {
        private const val SPLASH_DISPLAY_DURATION_MS = 2000L
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onRequestOverlayPermission: () -> Unit = {},
    onReleaseCharacter: (CharacterType) -> Unit = {},
    onStopCharacter: () -> Unit = {}
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedCharacter by remember { mutableStateOf(CharacterType.BLUE_TRIANGLE) }
    var isRoaming by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isRoaming) {
            Text(
                text = "'${selectedCharacter.label}'이(가) 원형 영역 안에서 돌아다니고 있어요",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "홈 화면이나 다른 앱으로 이동해도 캐릭터가 계속 보여요.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = {
                    onStopCharacter()
                    isRoaming = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("멈추기")
            }
        } else {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("텍스트 입력") },
                placeholder = { Text("내용을 입력하세요") }
            )

            Text(
                text = "캐릭터 선택",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CharacterType.entries.forEach { character ->
                    CharacterOption(
                        character = character,
                        selected = character == selectedCharacter,
                        onSelect = { selectedCharacter = character },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = "선택된 캐릭터: ${selectedCharacter.label}",
                style = MaterialTheme.typography.bodyLarge
            )

            if (inputText.isNotBlank()) {
                Text(
                    text = "입력된 텍스트: $inputText",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "버튼을 누르면 '다른 앱 위에 표시' 권한을 확인한 뒤, 앱이 내려가고 캐릭터가 동그란 원 안에서만 돌아다녀요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (Settings.canDrawOverlays(context)) {
                        isRoaming = true
                        onReleaseCharacter(selectedCharacter)
                    } else {
                        onRequestOverlayPermission()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("이 캐릭터 화면에 풀어두기")
            }
        }
    }
}

@Composable
fun CharacterOption(
    character: CharacterType,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.selectable(selected = selected, onClick = onSelect),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TriangleIcon(color = character.color, iconSize = 64.dp)
            Text(text = character.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TriangleIcon(
    color: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MobiMonTheme {
        MainScreen()
    }
}

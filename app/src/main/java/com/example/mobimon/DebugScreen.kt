package com.example.mobimon

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mobimon.feature.overlay.OverlayPetController
import com.example.mobimon.feature.pet.PetSelectionStore

@Composable
fun DebugScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val hasOverlayPermission = remember(refreshKey) { OverlayPetController.hasOverlayPermission(context) }
    val selectedCharacter = remember(refreshKey) { PetSelectionStore.getSelectedCharacter(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Debugging", style = MaterialTheme.typography.titleLarge)
        Text("오버레이 권한: ${if (hasOverlayPermission) "허용됨" else "허용 안 됨"}")
        Text("저장된 선택 Pet: ${selectedCharacter?.label ?: "없음"}")

        Button(onClick = {
            (context as? Activity)?.let(OverlayPetController::requestOverlayPermission)
        }) {
            Text("오버레이 권한 설정 열기")
        }

        OutlinedButton(onClick = {
            PetSelectionStore.clearSelectedCharacter(context)
            refreshKey++
        }) {
            Text("선택된 Pet 초기화 (다음 실행 시 선택 화면으로 이동)")
        }

        OutlinedButton(onClick = { refreshKey++ }) {
            Text("새로고침")
        }
    }
}

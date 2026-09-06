package com.example.mobimon.feature.pet

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mobimon.core.domain.pet.CharacterType
import com.example.mobimon.core.ui.theme.MobiMonTheme

/**
 * 펫 선택/성장 화면. 오버레이 권한이나 서비스 실행 여부는 이 화면이 알지 못하며,
 * 사용자의 의도([onReleaseRequested], [onStopRequested])만 상위(app 모듈)로 알린다.
 * 화면 전환에 필요한 [isRoaming] 상태는 상위에서 주입받는다(상태 호이스팅).
 */
@Composable
fun PetScreen(
    modifier: Modifier = Modifier,
    isRoaming: Boolean = false,
    onReleaseRequested: (CharacterType) -> Unit = {},
    onStopRequested: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var selectedCharacter by remember { mutableStateOf(CharacterType.BLUE_TRIANGLE) }

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
                onClick = onStopRequested,
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
                onClick = { onReleaseRequested(selectedCharacter) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("이 캐릭터 화면에 풀어두기")
            }
        }
    }
}

@Composable
private fun CharacterOption(
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
            TriangleIcon(color = Color(character.colorArgb), iconSize = 64.dp)
            Text(text = character.label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TriangleIcon(
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
private fun PetScreenPreview() {
    MobiMonTheme {
        PetScreen()
    }
}

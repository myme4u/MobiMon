package com.example.mobimon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSelectPet: () -> Unit = {},
    onTalkToPet: () -> Unit = {},
    onCheckPetInfo: () -> Unit = {},
    onDebug: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onSelectPet, modifier = Modifier.fillMaxWidth()) {
            Text("Pet 선택하기")
        }
        Button(onClick = onTalkToPet, modifier = Modifier.fillMaxWidth()) {
            Text("Pet과 대화하기")
        }
        Button(onClick = onCheckPetInfo, modifier = Modifier.fillMaxWidth()) {
            Text("Pet 정보 확인")
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(onClick = onDebug, modifier = Modifier.fillMaxWidth()) {
            Text("debugging")
        }
    }
}

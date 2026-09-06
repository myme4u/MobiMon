package com.example.mobimon.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

/**
 * 뒤로가기 버튼이 있는 공용 상단바. 시작 화면(Home)에서는 쓰지 않고,
 * 그 외 화면에서 app 모듈이 네비게이션 뒤로가기를 연결해 사용한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobiMonTopBar(
    onBack: () -> Unit,
    title: String = ""
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        }
    )
}

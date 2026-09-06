package com.example.mobimon.feature.voice

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object VoiceRoutes {
    const val CHAT = "pet_chat"
}

fun NavGraphBuilder.voiceChatGraph() {
    composable(VoiceRoutes.CHAT) {
        VoiceChatScreen(modifier = Modifier.fillMaxSize())
    }
}

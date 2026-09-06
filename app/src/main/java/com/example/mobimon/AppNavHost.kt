package com.example.mobimon

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobimon.core.domain.pet.CharacterType
import com.example.mobimon.feature.pet.PetRoutes
import com.example.mobimon.feature.pet.petSelectionGraph
import com.example.mobimon.feature.vehicleinfo.VehicleInfoRoutes
import com.example.mobimon.feature.vehicleinfo.vehicleInfoGraph
import com.example.mobimon.feature.voice.VoiceRoutes
import com.example.mobimon.feature.voice.voiceChatGraph

object AppRoutes {
    const val HOME = "home"
    const val DEBUG = "debug"
}

/**
 * 앱의 화면 조립 지점. 각 feature 모듈이 노출하는 NavGraphBuilder 확장 함수를
 * 호출만 하고, feature끼리 서로 참조하지 않도록 연결은 전부 여기서만 이루어진다.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    isRoaming: Boolean,
    onReleaseCharacterRequested: (CharacterType) -> Unit,
    onStopCharacterRequested: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onSelectPet = { navController.navigate(PetRoutes.PET_SELECTION) },
                onTalkToPet = { navController.navigate(VoiceRoutes.CHAT) },
                onCheckPetInfo = { navController.navigate(VehicleInfoRoutes.INFO) },
                onDebug = { navController.navigate(AppRoutes.DEBUG) }
            )
        }

        petSelectionGraph(
            isRoaming = isRoaming,
            onReleaseRequested = onReleaseCharacterRequested,
            onStopRequested = onStopCharacterRequested
        )

        voiceChatGraph()
        vehicleInfoGraph()

        composable(AppRoutes.DEBUG) {
            DebugScreen(modifier = Modifier.fillMaxSize())
        }
    }
}

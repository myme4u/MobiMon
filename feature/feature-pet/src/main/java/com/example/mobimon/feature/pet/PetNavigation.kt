package com.example.mobimon.feature.pet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mobimon.core.domain.pet.CharacterType

object PetRoutes {
    const val PET_SELECTION = "pet_selection"
}

fun NavGraphBuilder.petSelectionGraph(
    isRoaming: Boolean,
    onReleaseRequested: (CharacterType) -> Unit,
    onStopRequested: () -> Unit
) {
    composable(PetRoutes.PET_SELECTION) {
        PetScreen(
            modifier = Modifier.fillMaxSize(),
            isRoaming = isRoaming,
            onReleaseRequested = onReleaseRequested,
            onStopRequested = onStopRequested
        )
    }
}

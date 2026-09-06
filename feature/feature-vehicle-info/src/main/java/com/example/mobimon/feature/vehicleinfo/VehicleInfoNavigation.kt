package com.example.mobimon.feature.vehicleinfo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object VehicleInfoRoutes {
    const val INFO = "vehicle_info"
}

fun NavGraphBuilder.vehicleInfoGraph() {
    composable(VehicleInfoRoutes.INFO) {
        VehicleInfoScreen(modifier = Modifier.fillMaxSize())
    }
}

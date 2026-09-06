package com.example.mobimon.core.domain.pet

enum class CharacterType(val label: String, val colorArgb: Int) {
    BLUE_TRIANGLE("파란색 삼각형", 0xFF2196F3.toInt()),
    RED_TRIANGLE("빨간색 삼각형", 0xFFF44336.toInt())
}

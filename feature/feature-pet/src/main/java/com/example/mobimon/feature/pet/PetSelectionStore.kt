package com.example.mobimon.feature.pet

import android.content.Context
import com.example.mobimon.core.domain.pet.CharacterType

/**
 * 선택된 Pet을 기억해 다음 실행 때도 자동으로 홈 화면으로 진입할 수 있게 하는 저장소.
 * core-database(SQLite)가 구현되기 전까지의 임시 구현이며, 이후 그쪽으로 교체될 예정.
 */
object PetSelectionStore {
    private const val PREFS_NAME = "pet_selection"
    private const val KEY_SELECTED_CHARACTER = "selected_character"

    fun getSelectedCharacter(context: Context): CharacterType? {
        val name = prefs(context).getString(KEY_SELECTED_CHARACTER, null) ?: return null
        return CharacterType.entries.find { it.name == name }
    }

    fun setSelectedCharacter(context: Context, character: CharacterType) {
        prefs(context).edit().putString(KEY_SELECTED_CHARACTER, character.name).apply()
    }

    fun clearSelectedCharacter(context: Context) {
        prefs(context).edit().remove(KEY_SELECTED_CHARACTER).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

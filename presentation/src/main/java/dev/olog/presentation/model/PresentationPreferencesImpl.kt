package dev.olog.presentation.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class PresentationPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences
) : PresentationPreferencesGateway {

    companion object {
        private const val TAG = "AppPreferencesDataStoreImpl"

        private const val FIRST_ACCESS = "$TAG.FIRST_ACCESS"

        private const val BOTTOM_VIEW_LAST_PAGE = "$TAG.BOTTOM_VIEW_3"
    }

    override fun isFirstAccess(): Boolean {
        val isFirstAccess = preferences.getBoolean(FIRST_ACCESS, true)

        if (isFirstAccess) {
            preferences.edit { putBoolean(FIRST_ACCESS, false) }
        }

        return isFirstAccess
    }



    override fun getLastBottomViewPage(): BottomNavigationPage {
        val page =
            preferences.getString(BOTTOM_VIEW_LAST_PAGE, BottomNavigationPage.LIBRARY.toString())!!
        return BottomNavigationPage.valueOf(page)
    }

    override fun setLastBottomViewPage(page: BottomNavigationPage) {
        preferences.edit { putString(BOTTOM_VIEW_LAST_PAGE, page.toString()) }
    }

    override fun setDefault() {
        // TODO
//        setLibraryCategories(getDefaultLibraryCategories())
//        setPodcastLibraryCategories(getDefaultPodcastLibraryCategories())
    }

    override fun observePlayerControlsVisibility(): Flow<Boolean> {
        return preferences.observeKey(context.getString(dev.olog.prefskeys.R.string.prefs_player_controls_visibility_key), false)
    }

    override fun isAdaptiveColorEnabled(): Boolean {
        return preferences.getBoolean(context.getString(dev.olog.prefskeys.R.string.prefs_adaptive_colors_key), false)
    }


}
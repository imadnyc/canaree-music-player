package dev.olog.presentation.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.olog.core.MediaIdCategory
import dev.olog.presentation.R
import dev.olog.presentation.tab.TabCategory
import dev.olog.platform.extension.observeKey
import dev.olog.presentation.model.LibraryCategoryBehavior.*
import dev.olog.shared.assertBackgroundThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class PresentationPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences
) : PresentationPreferencesGateway {

    companion object {
        private const val TAG = "AppPreferencesDataStoreImpl"

        private const val FIRST_ACCESS = "$TAG.FIRST_ACCESS"

        private const val VIEW_PAGER_LAST_PAGE = "$TAG.VIEW_PAGER_LAST_PAGE"
        private const val VIEW_PAGER_PODCAST_LAST_PAGE = "$TAG.VIEW_PAGER_PODCAST_LAST_PAGE"
        private const val BOTTOM_VIEW_LAST_PAGE = "$TAG.BOTTOM_VIEW_3"
        private const val LIBRARY_LAST_PAGE = "$TAG.LIBRARY_PAGE"

        private const val CATEGORY_FOLDER_ORDER = "$TAG.CATEGORY_FOLDER_ORDER"
        private const val CATEGORY_PLAYLIST_ORDER = "$TAG.CATEGORY_PLAYLIST_ORDER"
        private const val CATEGORY_SONG_ORDER = "$TAG.CATEGORY_SONG_ORDER"
        private const val CATEGORY_ALBUM_ORDER = "$TAG.CATEGORY_ALBUM_ORDER"
        private const val CATEGORY_ARTIST_ORDER = "$TAG.CATEGORY_ARTIST_ORDER"
        private const val CATEGORY_GENRE_ORDER = "$TAG.CATEGORY_GENRE_ORDER"

        private const val CATEGORY_FOLDER_VISIBILITY = "$TAG.CATEGORY_FOLDER_VISIBILITY"
        private const val CATEGORY_PLAYLIST_VISIBILITY = "$TAG.CATEGORY_PLAYLIST_VISIBILITY"
        private const val CATEGORY_SONG_VISIBILITY = "$TAG.CATEGORY_SONG_VISIBILITY"
        private const val CATEGORY_ALBUM_VISIBILITY = "$TAG.CATEGORY_ALBUM_VISIBILITY"
        private const val CATEGORY_ARTIST_VISIBILITY = "$TAG.CATEGORY_ARTIST_VISIBILITY"
        private const val CATEGORY_GENRE_VISIBILITY = "$TAG.CATEGORY_GENRE_VISIBILITY"

        private const val CATEGORY_PODCAST_PLAYLIST_ORDER = "$TAG.CATEGORY_PODCAST_PLAYLIST_ORDER"
        private const val CATEGORY_PODCAST_ORDER = "$TAG.CATEGORY_PODCAST_ORDER"
        private const val CATEGORY_PODCAST_ALBUM_ORDER = "$TAG.CATEGORY_PODCAST_ALBUM_ORDER"
        private const val CATEGORY_PODCAST_ARTIST_ORDER = "$TAG.CATEGORY_PODCAST_ARTIST_ORDER"

        private const val CATEGORY_PODCAST_PLAYLIST_VISIBILITY =
            "$TAG.CATEGORY_PODCAST_PODCAST_PLAYLIST_VISIBILITY"
        private const val CATEGORY_PODCAST_VISIBILITY = "$TAG.CATEGORY_PODCAST_VISIBILITY"
        private const val CATEGORY_PODCAST_ALBUM_VISIBILITY =
            "$TAG.CATEGORY_PODCAST_ALBUM_VISIBILITY"
        private const val CATEGORY_PODCAST_ARTIST_VISIBILITY =
            "$TAG.CATEGORY_PODCAST_ARTIST_VISIBILITY"
    }

    override fun isFirstAccess(): Boolean {
        val isFirstAccess = preferences.getBoolean(FIRST_ACCESS, true)

        if (isFirstAccess) {
            preferences.edit { putBoolean(FIRST_ACCESS, false) }
        }

        return isFirstAccess
    }

    override fun getViewPagerLibraryLastPage(): Int {
        return preferences.getInt(VIEW_PAGER_LAST_PAGE, 2)
    }

    override fun setViewPagerLibraryLastPage(lastPage: Int) {
        preferences.edit { putInt(VIEW_PAGER_LAST_PAGE, lastPage) }
    }

    override fun getViewPagerPodcastLastPage(): Int {
        return preferences.getInt(VIEW_PAGER_PODCAST_LAST_PAGE, 1)
    }

    override fun setViewPagerPodcastLastPage(lastPage: Int) {
        preferences.edit { putInt(VIEW_PAGER_PODCAST_LAST_PAGE, lastPage) }
    }

    override fun getLastBottomViewPage(): BottomNavigationPage {
        val page =
            preferences.getString(BOTTOM_VIEW_LAST_PAGE, BottomNavigationPage.LIBRARY.toString())!!
        return BottomNavigationPage.valueOf(page)
    }

    override fun setLastBottomViewPage(page: BottomNavigationPage) {
        preferences.edit { putString(BOTTOM_VIEW_LAST_PAGE, page.toString()) }
    }

    override fun getLastLibraryPage(): LibraryPage {
        val page = preferences.getString(LIBRARY_LAST_PAGE, LibraryPage.TRACKS.toString())!!
        return LibraryPage.valueOf(page)
    }

    override fun setLibraryPage(page: LibraryPage) {
        preferences.edit {
            putString(LIBRARY_LAST_PAGE, page.toString())
        }
    }

    override fun getLibraryCategories(): List<LibraryCategoryBehavior> {
        return listOf(
            LibraryCategoryBehavior(
                Category.FOLDERS,
                preferences.getBoolean(CATEGORY_FOLDER_VISIBILITY, true),
                preferences.getInt(CATEGORY_FOLDER_ORDER, 0)
            ),
            LibraryCategoryBehavior(
                Category.PLAYLISTS,
                preferences.getBoolean(CATEGORY_PLAYLIST_VISIBILITY, true),
                preferences.getInt(CATEGORY_PLAYLIST_ORDER, 1)
            ),
            LibraryCategoryBehavior(
                Category.SONGS,
                preferences.getBoolean(CATEGORY_SONG_VISIBILITY, true),
                preferences.getInt(CATEGORY_SONG_ORDER, 2)
            ),
            LibraryCategoryBehavior(
                Category.ALBUMS,
                preferences.getBoolean(CATEGORY_ALBUM_VISIBILITY, true),
                preferences.getInt(CATEGORY_ALBUM_ORDER, 3)
            ),
            LibraryCategoryBehavior(
                Category.ARTISTS,
                preferences.getBoolean(CATEGORY_ARTIST_VISIBILITY, true),
                preferences.getInt(CATEGORY_ARTIST_ORDER, 4)
            ),
            LibraryCategoryBehavior(
                Category.GENRES,
                preferences.getBoolean(CATEGORY_GENRE_VISIBILITY, true),
                preferences.getInt(CATEGORY_GENRE_ORDER, 5)
            )
        ).sortedBy { it.order }
    }

    override fun getDefaultLibraryCategories(): List<LibraryCategoryBehavior> {
        return listOf(
            LibraryCategoryBehavior(Category.FOLDERS, true, 0),
            LibraryCategoryBehavior(Category.PLAYLISTS, true, 1),
            LibraryCategoryBehavior(Category.SONGS, true, 2),
            LibraryCategoryBehavior(Category.ALBUMS, true, 3),
            LibraryCategoryBehavior(Category.ARTISTS, true, 4),
            LibraryCategoryBehavior(Category.GENRES, true, 5),
        )
    }

    override fun setLibraryCategories(behavior: List<LibraryCategoryBehavior>) {
        preferences.edit {
            val folder = behavior.first { it.category == Category.FOLDERS }
            putInt(CATEGORY_FOLDER_ORDER, folder.order)
            putBoolean(CATEGORY_FOLDER_VISIBILITY, folder.visible)

            val playlist = behavior.first { it.category == Category.PLAYLISTS }
            putInt(CATEGORY_PLAYLIST_ORDER, playlist.order)
            putBoolean(CATEGORY_PLAYLIST_VISIBILITY, playlist.visible)

            val song = behavior.first { it.category == Category.SONGS }
            putInt(CATEGORY_SONG_ORDER, song.order)
            putBoolean(CATEGORY_SONG_VISIBILITY, song.visible)

            val album = behavior.first { it.category == Category.ALBUMS }
            putInt(CATEGORY_ALBUM_ORDER, album.order)
            putBoolean(CATEGORY_ALBUM_VISIBILITY, album.visible)

            val artist = behavior.first { it.category == Category.ARTISTS }
            putInt(CATEGORY_ARTIST_ORDER, artist.order)
            putBoolean(CATEGORY_ARTIST_VISIBILITY, artist.visible)

            val genre = behavior.first { it.category == Category.GENRES }
            putInt(CATEGORY_GENRE_ORDER, genre.order)
            putBoolean(CATEGORY_GENRE_VISIBILITY, genre.visible)
        }
    }

    override fun getPodcastLibraryCategories(): List<LibraryCategoryBehavior> {
        return listOf(
            LibraryCategoryBehavior(
                Category.PLAYLISTS,
                preferences.getBoolean(CATEGORY_PODCAST_PLAYLIST_VISIBILITY, true),
                preferences.getInt(CATEGORY_PODCAST_PLAYLIST_ORDER, 0)
            ),
            LibraryCategoryBehavior(
                Category.PODCASTS,
                preferences.getBoolean(CATEGORY_PODCAST_VISIBILITY, true),
                preferences.getInt(CATEGORY_PODCAST_ORDER, 1)
            ),
            LibraryCategoryBehavior(
                Category.ALBUMS,
                preferences.getBoolean(CATEGORY_PODCAST_ALBUM_VISIBILITY, true),
                preferences.getInt(CATEGORY_PODCAST_ALBUM_ORDER, 2)
            ),
            LibraryCategoryBehavior(
                Category.ARTISTS,
                preferences.getBoolean(CATEGORY_PODCAST_ARTIST_VISIBILITY, true),
                preferences.getInt(CATEGORY_PODCAST_ARTIST_ORDER, 3)
            )
        ).sortedBy { it.order }
    }

    override fun getDefaultPodcastLibraryCategories(): List<LibraryCategoryBehavior> {
        return listOf(
            LibraryCategoryBehavior(Category.PLAYLISTS, true, 0),
            LibraryCategoryBehavior(Category.SONGS, true, 1),
            LibraryCategoryBehavior(Category.ALBUMS, true, 2),
            LibraryCategoryBehavior(Category.ARTISTS, true, 3),
        )
    }

    override fun setPodcastLibraryCategories(behavior: List<LibraryCategoryBehavior>) {
        preferences.edit {

            val playlist = behavior.first { it.category == Category.PLAYLISTS }
            putInt(CATEGORY_PODCAST_PLAYLIST_ORDER, playlist.order)
            putBoolean(CATEGORY_PODCAST_PLAYLIST_VISIBILITY, playlist.visible)

            val song = behavior.first { it.category == Category.PODCASTS }
            putInt(CATEGORY_PODCAST_ORDER, song.order)
            putBoolean(CATEGORY_PODCAST_VISIBILITY, song.visible)

            val album = behavior.first { it.category == Category.ALBUMS }
            putInt(CATEGORY_PODCAST_ALBUM_ORDER, album.order)
            putBoolean(CATEGORY_PODCAST_ALBUM_VISIBILITY, album.visible)

            val artist = behavior.first { it.category == Category.ARTISTS }
            putInt(CATEGORY_PODCAST_ARTIST_ORDER, artist.order)
            putBoolean(CATEGORY_PODCAST_ARTIST_VISIBILITY, artist.visible)
        }
    }

    override fun setDefault() {
        assertBackgroundThread()
        setLibraryCategories(getDefaultLibraryCategories())
        setPodcastLibraryCategories(getDefaultPodcastLibraryCategories())
    }

    override fun observeLibraryNewVisibility(): Flow<Boolean> {
        return preferences.observeKey(
            context.getString(R.string.prefs_show_new_albums_artists_key),
            true
        )
    }

    override fun observeLibraryRecentPlayedVisibility(): Flow<Boolean> {
        return preferences.observeKey(
            (context.getString(R.string.prefs_show_recent_albums_artists_key)),
            true
        )
    }

    override fun observePlayerControlsVisibility(): Flow<Boolean> {
        return preferences.observeKey(context.getString(R.string.prefs_player_controls_visibility_key), false)
    }

    override fun isAdaptiveColorEnabled(): Boolean {
        assertBackgroundThread()
        return preferences.getBoolean(context.getString(R.string.prefs_adaptive_colors_key), false)
    }

    override fun getSpanCount(category: TabCategory): Int {
        return preferences.getInt("${category}_span", SpanCountController.getDefaultSpan(context, category))
    }

    override fun observeSpanCount(category: TabCategory): Flow<Int> {
        return preferences.observeKey("${category}_span", SpanCountController.getDefaultSpan(context, category))
    }

    override fun setSpanCount(category: TabCategory, spanCount: Int) {
        preferences.edit {
            putInt("${category}_span", spanCount)
        }
    }

    override fun canShowPodcasts(): Boolean {
        return preferences.getBoolean(context.getString(R.string.prefs_show_podcasts_key), true)
    }
}
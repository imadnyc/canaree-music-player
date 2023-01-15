package dev.olog.data.queries

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore.Audio.Media.*
import dev.olog.contentresolversql.querySql
import dev.olog.core.MediaIdCategory
import dev.olog.core.entity.sort.SortArranging
import dev.olog.core.entity.sort.SortType
import dev.olog.core.gateway.BlacklistGateway
import dev.olog.core.gateway.base.Id
import dev.olog.core.prefs.SortPreferences

@Suppress("DEPRECATION")
internal class AlbumsQueries(
    private val contentResolver: ContentResolver,
    blacklistPrefs: BlacklistGateway,
    sortPrefs: SortPreferences,
    isPodcast: Boolean
) : BaseQueries(blacklistPrefs, sortPrefs, isPodcast) {

    fun getAll(): Cursor {
        val (blacklist, params) = notBlacklisted()

        val query = """
             SELECT
                $ALBUM_ID,
                $ARTIST_ID,
                $ARTIST,
                $ALBUM,
                ${Columns.ALBUM_ARTIST},
                $DATA,
                $IS_PODCAST
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection(blacklist)}
            ORDER BY ${sortOrder()}
        """

        return contentResolver.querySql(query, params)
    }

    fun getSongList(id: Id): Cursor {
        val (blacklist, params) = notBlacklisted()

        val query = """
            SELECT $_ID, $ARTIST_ID, $ALBUM_ID,
                $TITLE, $ARTIST, $ALBUM, ${Columns.ALBUM_ARTIST},
                $DURATION, $DATA, $YEAR,
                $TRACK, $DATE_ADDED, $DATE_MODIFIED, $IS_PODCAST
            FROM $EXTERNAL_CONTENT_URI
            WHERE $ALBUM_ID = ? AND ${defaultSelection(blacklist)}
            ORDER BY ${songListSortOrder(MediaIdCategory.ALBUMS, DEFAULT_SORT_ORDER)}
        """
        return contentResolver.querySql(query, arrayOf("$id") + params)
    }

    fun getRecentlyAdded(): Cursor {
        val (blacklist, params) = notBlacklisted()

        val query = """
            SELECT 
                $ALBUM_ID,
                $ARTIST_ID,
                $ARTIST,
                $ALBUM,
                ${Columns.ALBUM_ARTIST},
                $DATA,
                $IS_PODCAST
            FROM $EXTERNAL_CONTENT_URI
            WHERE ${defaultSelection(blacklist)} AND ${isRecentlyAdded()}

            ORDER BY $DATE_ADDED DESC
        """
        return contentResolver.querySql(query, params)
    }

    private fun defaultSelection(notBlacklisted: String): String {
        return "${isPodcast()} AND $notBlacklisted"
    }

    private fun sortOrder(): String {
        if (isPodcast) {
            return "lower($ALBUM) COLLATE UNICODE"
        }

        val sortEntity = sortPrefs.getAllAlbumsSort()
        var sort = when (sortEntity.type) {
            SortType.ALBUM -> "lower($ALBUM)"
            SortType.ARTIST -> "lower($ARTIST)"
            SortType.ALBUM_ARTIST -> "lower(${Columns.ALBUM_ARTIST})"
            else -> "lower($ALBUM)"
        }

        sort += " COLLATE UNICODE "

        if (sortEntity.arranging == SortArranging.DESCENDING) {
            sort += " DESC"
        }
        return sort
    }

}
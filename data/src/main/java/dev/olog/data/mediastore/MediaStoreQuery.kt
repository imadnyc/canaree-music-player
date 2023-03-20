package dev.olog.data.mediastore

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Media
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.olog.platform.BuildVersion
import javax.inject.Inject

class MediaStoreQuery @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun queryAllAudio(): List<MediaStoreAudioInternalEntity> {
        val uri: Uri = when {
            BuildVersion.isQ() -> Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else -> Media.EXTERNAL_CONTENT_URI
        }
        val selection = buildString {
            append("${AudioColumns.IS_ALARM} = 0")
            if (BuildVersion.isQ()) {
                append(" AND ${AudioColumns.IS_AUDIOBOOK} = 0")
            }
            append(" AND ${AudioColumns.IS_NOTIFICATION} = 0")
            if (BuildVersion.isS()) {
                append(" AND ${AudioColumns.IS_RECORDING} = 0")
            }
            append(" AND ${AudioColumns.IS_RINGTONE} = 0")
        }

        val cursor = context.contentResolver.query(uri, null, selection, null, null) ?: return emptyList()
        try {
            val result = mutableListOf<MediaStoreAudioInternalEntity>()

            val idColumn = cursor.getColumnIndexOrThrow(AudioColumns._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(AudioColumns.ALBUM)
            val albumArtistColumn = cursor.getColumnIndexOrThrow(AudioColumns.ALBUM_ARTIST)
            val artistColumn = cursor.getColumnIndexOrThrow(AudioColumns.ARTIST)
            val bitrateColumn = cursor.getColumnIndexOrThrow(AudioColumns.BITRATE)
            val bucketDisplayNameColumn = cursor.getColumnIndexOrThrow(AudioColumns.BUCKET_DISPLAY_NAME)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(AudioColumns.BUCKET_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(AudioColumns.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(AudioColumns.DATE_ADDED)
            val displayNameColumn = cursor.getColumnIndexOrThrow(AudioColumns.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(AudioColumns.DURATION)
            val isFavoriteColumn = cursor.getColumnIndexOrThrow(AudioColumns.IS_FAVORITE)
            val relativePathColumn = cursor.getColumnIndexOrThrow(AudioColumns.RELATIVE_PATH)
            val sizeColumn = cursor.getColumnIndexOrThrow(AudioColumns.SIZE)
            val titleColumn = cursor.getColumnIndexOrThrow(AudioColumns.TITLE)
            val yearColumn = cursor.getColumnIndexOrThrow(AudioColumns.YEAR)
            val albumIdColumn = cursor.getColumnIndexOrThrow(AudioColumns.ALBUM_ID)
            val artistIdColumn = cursor.getColumnIndexOrThrow(AudioColumns.ARTIST_ID)
            val bookmarkColumn = cursor.getColumnIndexOrThrow(AudioColumns.BOOKMARK)
            val isPodcastColumn = cursor.getColumnIndexOrThrow(AudioColumns.IS_PODCAST)
            val trackColumn = cursor.getColumnIndexOrThrow(AudioColumns.TRACK)
            val authorColumn = cursor.getColumnIndexOrThrow(AudioColumns.AUTHOR)
            val compilationColumn = cursor.getColumnIndexOrThrow(AudioColumns.COMPILATION)
            val composerColumn = cursor.getColumnIndexOrThrow(AudioColumns.COMPOSER)
            val writerColumn = cursor.getColumnIndexOrThrow(AudioColumns.WRITER)

            while (cursor.moveToNext()) {
                result += MediaStoreAudioInternalEntity(
                    id = cursor.getLong(idColumn),
                    album = cursor.getStringOrNull(albumColumn),
                    albumArtist = cursor.getStringOrNull(albumArtistColumn),
                    artist = cursor.getStringOrNull(artistColumn),
                    bitrate = cursor.getInt(bitrateColumn),
                    bucketDisplayName = cursor.getStringOrNull(bucketDisplayNameColumn) ?: MediaStore.UNKNOWN_STRING,
                    bucketId = cursor.getLong(bucketIdColumn),
                    data = cursor.getStringOrNull(dataColumn),
                    dateAdded = cursor.getLong(dateAddedColumn),
                    displayName = cursor.getString(displayNameColumn),
                    duration = cursor.getLong(durationColumn),
                    isFavorite = cursor.getInt(isFavoriteColumn),
                    relativePath = cursor.getString(relativePathColumn),
                    size = cursor.getLong(sizeColumn),
                    title = cursor.getString(titleColumn),
                    year = cursor.getIntOrNull(yearColumn),
                    albumId = cursor.getLong(albumIdColumn),
                    artistId = cursor.getLong(artistIdColumn),
                    bookmark = cursor.getIntOrNull(bookmarkColumn),
                    isPodcast = cursor.getInt(isPodcastColumn),
                    track = cursor.getIntOrNull(trackColumn),
                    author = cursor.getStringOrNull(authorColumn),
                    compilation = cursor.getStringOrNull(compilationColumn),
                    composer = cursor.getStringOrNull(composerColumn),
                    writer = cursor.getStringOrNull(writerColumn),
                )
            }

            return result
        } finally {
            cursor.close()
        }
    }

    // TODO check if works with _ID as well
    fun getDisplayNameByUri(uri: Uri): String? {
        // https://developer.android.com/training/secure-file-sharing/retrieve-info
        // content uri has only two field [_id, _display_name]
        return context.contentResolver.query(
            uri,
            arrayOf(AudioColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use {
            it.moveToFirst()
            it.getStringOrNull(0)
        }
    }

}
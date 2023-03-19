package dev.olog.data.repository.track

import android.net.Uri
import dev.olog.core.entity.track.Song
import dev.olog.core.gateway.track.SongGateway
import dev.olog.data.mediastore.toSong
import dev.olog.data.queries.AudioQueries
import dev.olog.shared.mapListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SongRepository @Inject constructor(
    private val queries: AudioQueries,
) : SongGateway {

    override fun getAll(): List<Song> {
        return queries.getAll(false).map { it.toSong() }
    }

    override fun observeAll(): Flow<List<Song>> {
        return queries.observeAll(false)
            .mapListItem { it.toSong() }
    }

    override fun getByParam(id: Long): Song? {
        return queries.getById(id)?.toSong()
    }

    override fun observeByParam(id: Long): Flow<Song?> {
        return queries.observeById(id).map { it?.toSong() }
    }

    override suspend fun deleteSingle(id: Long) {
        TODO()
//        return deleteInternal(id)
    }

    override suspend fun deleteGroup(ids: List<Song>) {
        TODO()
//        for (id in ids) {
//            deleteInternal(id.id)
//        }
    }

    private fun deleteInternal(id: Long) {
        TODO()
//        assertBackgroundThread()
//        val path = getByParam(id)!!.path
//        val uri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI, id)
//        val deleted = contentResolver.delete(uri, null, null)
//        if (deleted < 1) {
//            Log.w("SongRepo", "song not found $id")
//            return
//        }
//
//        val file = File(path)
//        if (file.exists()) {
//            file.delete()
//        }
    }

    override fun getByUri(uri: Uri): Song? {
        TODO()
//        try {
//            val id = getByUriInternal(uri) ?: return null
//            return getByParam(id)
//        } catch (ex: Exception){
//            ex.printStackTrace()
//            return null
//        }
    }

    private fun getByUriInternal(uri: Uri): Long? {
        TODO()
        // https://developer.android.com/training/secure-file-sharing/retrieve-info
        // content uri has only two field [_id, _display_name]
//        val fileQuery = """
//            SELECT ${Audio.Media.DISPLAY_NAME}
//            FROM $uri
//        """
//        val displayName = contentResolver.querySql(fileQuery).use {
//            it.moveToFirst()
//            it.getString(Audio.Media.DISPLAY_NAME)
//        }
//
//        val itemQuery = """
//            SELECT ${Audio.Media._ID}
//            FROM ${Audio.Media.EXTERNAL_CONTENT_URI}
//            WHERE ${Audio.Media.DISPLAY_NAME} = ?
//        """
//        val id = contentResolver.querySql(itemQuery, arrayOf(displayName)).use {
//            it.moveToFirst()
//            it.getLong(Audio.Media._ID)
//        }
//        return id
    }

    override fun getByAlbumId(albumId: Long): Song? {
        return queries.getByAlbumId(albumId)?.toSong()
    }
}
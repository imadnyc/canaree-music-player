package dev.olog.data.repository

import dev.olog.domain.entity.AutoPlaylist
import dev.olog.domain.entity.favorite.FavoriteType
import dev.olog.domain.gateway.FavoriteGateway
import dev.olog.domain.gateway.track.PlaylistOperations
import dev.olog.data.local.history.HistoryDao
import dev.olog.data.local.playlist.PlaylistDao
import dev.olog.data.local.playlist.PlaylistEntity
import dev.olog.data.local.playlist.PlaylistTrackEntity
import dev.olog.shared.swap
import javax.inject.Inject

internal class PlaylistRepositoryHelper @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val historyDao: HistoryDao,
    private val favoriteGateway: FavoriteGateway

) : PlaylistOperations {


    override suspend fun createPlaylist(playlistName: String): Long {
        return playlistDao.createPlaylist(
            PlaylistEntity(name = playlistName, size = 0)
        )
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        var maxIdInPlaylist = (playlistDao.getPlaylistMaxId(playlistId) ?: 1).toLong()
        val tracks = songIds.map {
            PlaylistTrackEntity(
                playlistId = playlistId, idInPlaylist = ++maxIdInPlaylist,
                trackId = it
            )
        }
        playlistDao.insertTracks(tracks)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        return playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun clearPlaylist(playlistId: Long) {
        require(AutoPlaylist.isAutoPlaylist(playlistId))
        when (playlistId) {
            AutoPlaylist.FAVORITE.id -> return favoriteGateway.deleteAll(FavoriteType.TRACK)
            AutoPlaylist.HISTORY.id -> return historyDao.deleteAll()
        }
    }

    override suspend fun removeFromPlaylist(playlistId: Long, idInPlaylist: Long) {
        if (AutoPlaylist.isAutoPlaylist(playlistId)) {
            removeFromAutoPlaylist(playlistId, idInPlaylist)
        } else {
            return playlistDao.deleteTrack(playlistId, idInPlaylist)
        }
    }

    private suspend fun removeFromAutoPlaylist(playlistId: Long, songId: Long) {
        return when (playlistId) {
            AutoPlaylist.FAVORITE.id -> favoriteGateway.deleteSingle(FavoriteType.TRACK, songId)
            AutoPlaylist.HISTORY.id -> historyDao.deleteSingle(songId)
            else -> throw IllegalArgumentException("invalid auto playlist id: $playlistId")
        }
    }

    override suspend fun renamePlaylist(playlistId: Long, newTitle: String) {
        return playlistDao.renamePlaylist(playlistId, newTitle)
    }

    override suspend fun moveItem(
        playlistId: Long,
        moveList: List<Pair<Int, Int>>
    ) {
        val trackList = playlistDao.getPlaylistTracksImpl(playlistId).toMutableList()
        for ((from, to) in moveList) {
            trackList.swap(from, to)
        }
        val result = trackList.mapIndexed { index, entity -> entity.copy(idInPlaylist = index.toLong()) }
        playlistDao.updateTrackList(result)
    }

    override suspend fun removeDuplicated(playlistId: Long) {
        val notDuplicate = playlistDao.getPlaylistTracksImpl(playlistId)
            .groupBy { it.trackId }
            .map { it.value[0] }
        playlistDao.deletePlaylistTracks(playlistId)
        playlistDao.insertTracks(notDuplicate)
    }

    override suspend fun insertSongToHistory(songId: Long) {
        return historyDao.insert(songId)
    }

}
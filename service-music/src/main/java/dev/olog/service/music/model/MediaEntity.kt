package dev.olog.service.music.model

import dev.olog.core.MediaId
import dev.olog.core.entity.PlayingQueueSong
import dev.olog.core.entity.track.Song

internal data class MediaEntity(
    val id: Long,
    val idInPlaylist: Int,
    val mediaId: MediaId,
    val artistId: Long,
    val albumId: Long,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val album: String,
    val duration: Long,
    val dateAdded: Long,
    val path: String,
    val discNumber: Int,
    val trackNumber: Int,
    val isPodcast: Boolean
)

internal fun Song.toMediaEntity(progressive: Int, mediaId: MediaId) : MediaEntity {
    return MediaEntity(
        id = this.id,
        idInPlaylist = progressive,
        mediaId = MediaId.playableItem(mediaId, this.id),
        artistId = this.artistId,
        albumId = this.albumId,
        title = this.title,
        artist = this.artist,
        albumArtist = this.albumArtist,
        album = this.album,
        duration = this.duration,
        dateAdded = this.dateAdded,
        path = this.path,
        discNumber = this.discNumber,
        trackNumber = this.trackNumber,
        isPodcast = this.isPodcast
    )
}

internal fun PlayingQueueSong.toMediaEntity() : MediaEntity {
    val song = this.song
    return MediaEntity(
        id = song.id,
        idInPlaylist = song.idInPlaylist,
        mediaId = song.getMediaId(),
        artistId = song.artistId,
        albumId = song.albumId,
        title = song.title,
        artist = song.artist,
        albumArtist = song.albumArtist,
        album = song.album,
        duration = song.duration,
        dateAdded = song.dateAdded,
        path = song.path,
        discNumber = song.discNumber,
        trackNumber = song.trackNumber,
        isPodcast = song.isPodcast
    )
}

internal fun MediaEntity.toPlayerMediaEntity(positionInQueue: PositionInQueue, bookmark: Long) : PlayerMediaEntity {
    return PlayerMediaEntity(this, positionInQueue, bookmark)
}
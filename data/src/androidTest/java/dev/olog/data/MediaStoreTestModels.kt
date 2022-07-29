package dev.olog.data

import dev.olog.data.mediastore.MediaStoreAudioEntity
import dev.olog.data.mediastore.song.MediaStoreSongsView
import dev.olog.data.mediastore.song.MediaStoreSongsViewSorted
import dev.olog.data.mediastore.song.album.MediaStoreAlbumsView
import dev.olog.data.mediastore.song.album.MediaStoreAlbumsViewSorted
import dev.olog.data.mediastore.song.artist.MediaStoreArtistsView
import dev.olog.data.mediastore.song.artist.MediaStoreArtistsViewSorted

fun emptyMediaStoreAudioEntity(
    id: String = "",
    artistId: String = "",
    albumId: String = "",
    title: String = "",
    artist: String = "",
    albumArtist: String = "",
    album: String = "",
    duration: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    directory: String = "",
    path: String = "",
    discNumber: Int = 0,
    trackNumber: Int = 0,
    isPodcast: Boolean,
    displayName: String = "",
) = MediaStoreAudioEntity(
    id = id,
    artistId = artistId,
    albumId = albumId,
    title = title,
    artist = artist,
    albumArtist = albumArtist,
    album = album,
    duration = duration,
    dateAdded = dateAdded,
    dateModified = dateModified,
    directory = directory,
    path = path,
    discNumber = discNumber,
    trackNumber = trackNumber,
    isPodcast = isPodcast,
    displayName = displayName
)

fun emptyMediaStoreSongView(
    id: String = "",
    artistId: String = "",
    albumId: String = "",
    title: String = "",
    artist: String = "",
    albumArtist: String = "",
    album: String = "",
    duration: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    directory: String = "",
    path: String = "",
    discNumber: Int = 0,
    trackNumber: Int = 0,
    isPodcast: Boolean,
    displayName: String = "",
) = MediaStoreSongsView(
    id = id,
    artistId = artistId,
    albumId = albumId,
    title = title,
    artist = artist,
    albumArtist = albumArtist,
    album = album,
    duration = duration,
    dateAdded = dateAdded,
    dateModified = dateModified,
    directory = directory,
    path = path,
    discNumber = discNumber,
    trackNumber = trackNumber,
    isPodcast = isPodcast,
    displayName = displayName
)

fun emptyMediaStoreSongSortedView(
    id: String = "",
    artistId: String = "",
    albumId: String = "",
    title: String = "",
    artist: String = "",
    albumArtist: String = "",
    album: String = "",
    duration: Long = 0,
    dateAdded: Long = 0,
    dateModified: Long = 0,
    directory: String = "",
    path: String = "",
    discNumber: Int = 0,
    trackNumber: Int = 0,
    isPodcast: Boolean,
    displayName: String = "",
) = MediaStoreSongsViewSorted(
    id = id,
    artistId = artistId,
    albumId = albumId,
    title = title,
    artist = artist,
    albumArtist = albumArtist,
    album = album,
    duration = duration,
    dateAdded = dateAdded,
    dateModified = dateModified,
    directory = directory,
    path = path,
    discNumber = discNumber,
    trackNumber = trackNumber,
    isPodcast = isPodcast,
    displayName = displayName
)

fun emptyMediaStoreArtistView(
    id: String = "",
    name: String = "",
    songs: Int = 0,
    dateAdded: Long = 0,
) = MediaStoreArtistsView(
    id = id,
    name = name,
    songs = songs,
    dateAdded = dateAdded,
)

fun emptyMediaStoreArtistSortedView(
    id: String = "",
    name: String = "",
    songs: Int = 0,
    dateAdded: Long = 0,
) = MediaStoreArtistsViewSorted(
    id = id,
    name = name,
    songs = songs,
    dateAdded = dateAdded,
)

fun emptyMediaStoreAlbumView(
    id: String = "",
    artistId: String = "",
    title: String = "",
    artist: String = "",
    albumArtist: String = "",
    songs: Int = 0,
    dateAdded: Long = 0,
    directory: String = "",
) = MediaStoreAlbumsView(
    id = id,
    artistId = artistId,
    title = title,
    artist = artist,
    albumArtist = albumArtist,
    songs = songs,
    dateAdded = dateAdded,
    directory = directory,
)

fun emptyMediaStoreAlbumSortedView(
    id: String = "",
    artistId: String = "",
    title: String = "",
    artist: String = "",
    albumArtist: String = "",
    songs: Int = 0,
    dateAdded: Long = 0,
    directory: String = "",
) = MediaStoreAlbumsViewSorted(
    id = id,
    artistId = artistId,
    title = title,
    artist = artist,
    albumArtist = albumArtist,
    songs = songs,
    dateAdded = dateAdded,
    directory = directory,
)
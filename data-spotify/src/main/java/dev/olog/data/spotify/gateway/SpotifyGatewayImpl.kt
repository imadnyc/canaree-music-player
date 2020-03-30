package dev.olog.data.spotify.gateway

import dev.olog.core.MediaId
import dev.olog.core.entity.spotify.SpotifyAlbum
import dev.olog.core.entity.spotify.SpotifyAlbumType
import dev.olog.core.entity.spotify.SpotifyTrack
import dev.olog.core.entity.track.Album
import dev.olog.core.entity.track.Artist
import dev.olog.core.gateway.spotify.SpotifyGateway
import dev.olog.core.gateway.track.AlbumGateway
import dev.olog.core.gateway.track.ArtistGateway
import dev.olog.data.shared.retrofit.IoResult
import dev.olog.data.shared.retrofit.fix
import dev.olog.data.shared.retrofit.flatMap
import dev.olog.data.shared.retrofit.map
import dev.olog.data.spotify.db.SpotifyImageEntity
import dev.olog.data.spotify.db.SpotifyImagesDao
import dev.olog.data.spotify.entity.RemoteSpotifyAlbum
import dev.olog.data.spotify.entity.RemoteSpotifyArtist
import dev.olog.data.spotify.entity.RemoteSpotifyTrack
import dev.olog.data.spotify.service.SpotifyService
import dev.olog.shared.throwNotHandled
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.NoSuchElementException

internal class SpotifyGatewayImpl @Inject constructor(
    private val artistGateway: ArtistGateway,
    private val albumGateway: AlbumGateway,
    private val service: SpotifyService,
    private val imageDao: SpotifyImagesDao
) : SpotifyGateway {

    override suspend fun getArtistAlbums(
        artistMediaId: MediaId.Category,
        type: SpotifyAlbumType
    ): List<SpotifyAlbum> {
        val artist = artistGateway.getByParam(artistMediaId.categoryId.toLong())!!

        return findSpotifyArtistBestMatch(artist)
            .flatMap { service.getArtistAlbums(it.id, type.value) }
            .map { artistAlbums ->
                artistAlbums.items.map { it.toDomain() }.distinctBy { it.title }
            }
            .fix(orDefault = emptyList())
            .also { albums ->
                imageDao.insertImages(albums.map { SpotifyImageEntity(it.uri, it.image) })
            }
    }

    override suspend fun getArtistTopTracks(artistMediaId: MediaId.Category): List<SpotifyTrack> {
        val artist = artistGateway.getByParam(artistMediaId.categoryId.toLong())!!

        return findSpotifyArtistBestMatch(artist)
            .flatMap { service.getArtistTopTracks(it.id) }
            .map { topTracks ->
                topTracks.tracks.map { it.toDomain() }
            }
            .fix(orDefault = emptyList())
            .also { tracks ->
                imageDao.insertImages(tracks.map { SpotifyImageEntity(it.uri, it.image) })
            }
    }

    override fun getImage(spotifyUri: String): String? {
        return imageDao.getImage(spotifyUri)
    }

    override suspend fun getAlbumTracks(albumMediaId: MediaId.Category): List<SpotifyTrack> {
        val album = albumGateway.getByParam(albumMediaId.categoryId.toLong())!!

        return findSpotifyAlbumBestMatch(album)
            .flatMap { service.getAlbumTracks(it.id) }
            .map { it.items }
            .map { tracks ->
                tracks.map { it.toDomain() }
            }.fix(orDefault = emptyList())
            .also { tracks ->
                imageDao.insertImages(tracks.map { SpotifyImageEntity(it.uri, it.image) })
            }
    }

    private suspend fun findSpotifyArtistBestMatch(artist: Artist): IoResult<RemoteSpotifyArtist> {
        try {
            return service.searchArtist("artist:${artist.name}")
                .map { it.artists.items }
                .map { artists ->
                    val bestIndex = FuzzySearch.extractOne(artist.name, artists.map { it.name }).index
                    artists[bestIndex]
                }
        } catch (ex: NoSuchElementException) {
            return IoResult.Error.Generic(ex)
        }
    }

    private suspend fun findSpotifyAlbumBestMatch(album: Album): IoResult<RemoteSpotifyAlbum> {
        try {
            return service.searchAlbum("album:${album.title} artist:${album.artist}")
                .map { it.albums.items }
                .map { albums ->
                    val bestIndex = FuzzySearch.extractOne(album.title, albums.map { it.name }).index
                    albums[bestIndex]
                }
        } catch (ex: NoSuchElementException) {
            return IoResult.Error.Generic(ex)
        }
    }

    private fun RemoteSpotifyTrack.toDomain(): SpotifyTrack {
        return SpotifyTrack(
            id = this.id,
            name = this.name,
            uri = this.uri,
            image = this.album?.images?.maxBy { it.height }?.url ?: "",
            discNumber = this.disc_number,
            trackNumber = this.track_number,
            duration = this.duration_ms.toLong(),
            isExplicit = this.explicit
        )
    }

    private fun RemoteSpotifyAlbum.toDomain(): SpotifyAlbum {
        return SpotifyAlbum(
            id = this.id,
            title = this.name,
            albumType = this.album_type.mapAlbumType(),
            image = this.images.maxBy { it.height }!!.url,
            songs = this.total_tracks,
            uri = this.uri
        )
    }

    private fun String.mapAlbumType(): SpotifyAlbumType {
        return when (this) {
            "album" -> SpotifyAlbumType.ALBUM
            "single" -> SpotifyAlbumType.SINGLE
            "appears_on" -> SpotifyAlbumType.APPEARS_ON
            "compilation" -> SpotifyAlbumType.COMPILATION
            else -> throwNotHandled(this)
        }
    }

}
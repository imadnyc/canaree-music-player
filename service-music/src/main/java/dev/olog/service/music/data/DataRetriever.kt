package dev.olog.service.music.data

import dagger.hilt.android.scopes.ServiceScoped
import dev.olog.core.MediaId
import dev.olog.core.MediaIdModifier
import dev.olog.core.entity.PlayingQueueTrack
import dev.olog.core.entity.track.Track
import dev.olog.core.gateway.PlayingQueueGateway
import dev.olog.core.gateway.track.GenreGateway
import dev.olog.core.gateway.track.SongGateway
import dev.olog.core.interactor.ObserveMostPlayedSongsUseCase
import dev.olog.core.interactor.ObserveRecentlyAddedUseCase
import dev.olog.core.interactor.songlist.GetSongListByParamUseCase
import dev.olog.intents.MusicServiceCustomAction
import dev.olog.service.music.model.MediaEntity
import dev.olog.service.music.model.toMediaEntity
import dev.olog.service.music.queue.EnhancedShuffle
import dev.olog.service.music.voice.VoiceSearch
import dev.olog.service.music.voice.VoiceSearchParams
import dev.olog.shared.android.BundleDictionary
import dev.olog.shared.exhaustive
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

@ServiceScoped
internal class DataRetriever @Inject constructor(
    private val playingQueueGateway: PlayingQueueGateway,
    private val songGateway: SongGateway,
    private val genreGateway: GenreGateway,
    private val getSongListByParamUseCase: GetSongListByParamUseCase,
    private val enhancedShuffle: EnhancedShuffle,
    private val getMostPlayedSongsUseCase: ObserveMostPlayedSongsUseCase,
    private val getRecentlyAddedUseCase: ObserveRecentlyAddedUseCase,
    private val voiceSearch: VoiceSearch,
) {

    suspend fun getLastQueue(): List<MediaEntity> {
        return playingQueueGateway.getAll().map(PlayingQueueTrack::toMediaEntity)
    }

    suspend fun getFromMediaId(
        mediaId: MediaId,
        extras: BundleDictionary?,
    ): List<MediaEntity> {
        Timber.v("getFromMediaId mediaId=$mediaId, extras=$extras")
        return when (mediaId.modifier) {
            MediaIdModifier.MOST_PLAYED -> fetchMostPlayed(mediaId)
            MediaIdModifier.RECENTLY_ADDED -> fetchRecentlyAdded(mediaId)
            MediaIdModifier.SHUFFLE -> fetchShuffle(mediaId, extras)
            null -> fetchFromMediaId(mediaId, extras)
        }.exhaustive
    }

    private suspend fun fetchFromMediaId(
        mediaId: MediaId,
        extras: BundleDictionary?
    ): List<MediaEntity> {
        val filter = extras?.getTyped<String>(MusicServiceCustomAction.ARGUMENT_FILTER)
        return getSongListByParamUseCase(mediaId).asSequence()
            .filterSongList(filter)
            .mapIndexed { index, track -> track.toMediaEntity(index, mediaId) }
            .toList()
    }

    private suspend fun fetchShuffle(
        mediaId: MediaId,
        extras: BundleDictionary?
    ): List<MediaEntity> {
        val filter = extras?.getTyped<String>(MusicServiceCustomAction.ARGUMENT_FILTER)

        val items =  getSongListByParamUseCase(mediaId).asSequence()
            .filterSongList(filter)
            .mapIndexed { index, track -> track.toMediaEntity(index, mediaId) }
            .toList()

        return enhancedShuffle(items)
    }

    private suspend fun fetchRecentlyAdded(
        mediaId: MediaId
    ): List<MediaEntity> {
        return getRecentlyAddedUseCase(mediaId).first()
            .mapIndexed { index, track -> track.toMediaEntity(index, mediaId) }
    }

    private suspend fun fetchMostPlayed(
        mediaId: MediaId
    ): List<MediaEntity> {
        return getMostPlayedSongsUseCase(mediaId).first()
            .mapIndexed { index, track -> track.toMediaEntity(index, mediaId) }
    }

    suspend fun getFromSearch(
        query: String?,
        extras: BundleDictionary,
    ): List<MediaEntity> {
        query ?: return emptyList()

        val params = VoiceSearchParams(query, extras)

        val mediaId = MediaId.songId(-1)

        return when {
            params.isUnstructured -> voiceSearch.search(
                songList = getSongListByParamUseCase(mediaId),
                query = query
            )
            params.isAlbumFocus -> voiceSearch.filterByAlbum(
                songList = getSongListByParamUseCase(mediaId),
                query = params.album
            )
            params.isArtistFocus -> voiceSearch.filterByArtist(
                songList = getSongListByParamUseCase(mediaId),
                query = params.artist
            )
            params.isSongFocus -> voiceSearch.filterByTrack(
                songList = getSongListByParamUseCase(mediaId),
                query = params.track
            )
            params.isGenreFocus -> voiceSearch.filterByGenre(
                genreGateway = genreGateway,
                query = params.genre
            )
            else -> voiceSearch.noFilter(
                songList = getSongListByParamUseCase(mediaId)
            )
        }
    }

    suspend fun getFromUri(
        uri: URI,
        extras: BundleDictionary,
    ): List<MediaEntity> {
        val track = songGateway.getByUri(uri) ?: return emptyList()
        val mediaEntity = track.toMediaEntity(
            progressive = 0,
            mediaId = track.getMediaId() // TODO not sure this mapping is correct
        )

        return listOf(mediaEntity)
    }

    private fun Sequence<Track>.filterSongList(filter: String?): Sequence<Track> {
        return this.filter {
            if (filter.isNullOrBlank()) {
                true
            } else {
                it.title.contains(filter, true) ||
                    it.artist.contains(filter, true) ||
                    it.album.contains(filter, true)
            }
        }
    }

}
package dev.olog.data.author

import dev.olog.core.MediaStoreType
import dev.olog.core.author.Artist
import dev.olog.core.author.AuthorGateway
import dev.olog.core.MediaUri
import dev.olog.core.schedulers.Schedulers
import dev.olog.core.sort.AuthorDetailSort
import dev.olog.core.sort.AuthorSort
import dev.olog.core.sort.Sort
import dev.olog.core.track.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AuthorRepository @Inject constructor(
    private val schedulers: Schedulers,
    private val repository: MediaStoreArtistRepository,
    private val podcastRepository: MediaStorePodcastAuthorRepository,
) : AuthorGateway {

    override fun getAll(type: MediaStoreType): List<Artist> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.getAll()
            MediaStoreType.Song -> repository.getAll()
        }
    }

    override fun observeAll(type: MediaStoreType): Flow<List<Artist>> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.observeAll()
            MediaStoreType.Song -> repository.observeAll()
        }
    }

    override fun getById(uri: MediaUri): Artist? {
        return when (uri.isPodcast) {
            true -> podcastRepository.getById(uri.id)
            false -> repository.getById(uri.id)
        }
    }

    override fun observeById(uri: MediaUri): Flow<Artist?> {
        return when (uri.isPodcast) {
            true -> podcastRepository.observeById(uri.id)
            false -> repository.observeById(uri.id)
        }
    }

    override fun getTracksById(uri: MediaUri): List<Song> {
        return when (uri.isPodcast) {
            true -> podcastRepository.getTracksById(uri.id)
            false -> repository.getTracksById(uri.id)
        }
    }

    override fun observeTracksById(uri: MediaUri): Flow<List<Song>> {
        return when (uri.isPodcast) {
            true -> podcastRepository.observeTracksById(uri.id)
            false -> repository.observeTracksById(uri.id)
        }
    }

    override fun observeRecentlyPlayed(type: MediaStoreType): Flow<List<Artist>> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.observeRecentlyPlayed()
            MediaStoreType.Song -> repository.observeRecentlyPlayed()
        }
    }

    override suspend fun addToRecentlyPlayed(uri: MediaUri) = withContext(schedulers.io) {
        when (uri.isPodcast) {
            true -> podcastRepository.addToRecentlyPlayed(uri.id)
            false -> repository.addToRecentlyPlayed(uri.id)
        }
    }

    override fun observeRecentlyAdded(type: MediaStoreType): Flow<List<Artist>> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.observeRecentlyAdded()
            MediaStoreType.Song -> repository.observeRecentlyAdded()
        }
    }

    override fun getSort(type: MediaStoreType): Sort<AuthorSort> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.getSort()
            MediaStoreType.Song -> repository.getSort()
        }
    }

    override fun setSort(type: MediaStoreType, sort: Sort<AuthorSort>) {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.setSort(sort)
            MediaStoreType.Song -> repository.setSort(sort)
        }
    }

    override fun getDetailSort(type: MediaStoreType): Sort<AuthorDetailSort> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.getDetailSort()
            MediaStoreType.Song -> repository.getDetailSort()
        }
    }

    override fun observeDetailSort(type: MediaStoreType): Flow<Sort<AuthorDetailSort>> {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.observeDetailSort()
            MediaStoreType.Song -> repository.observeDetailSort()
        }
    }

    override fun setDetailSort(type: MediaStoreType, sort: Sort<AuthorDetailSort>) {
        return when (type) {
            MediaStoreType.Podcast -> podcastRepository.setDetailSort(sort)
            MediaStoreType.Song -> repository.setDetailSort(sort)
        }
    }

}
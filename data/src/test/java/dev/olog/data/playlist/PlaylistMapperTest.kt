package dev.olog.data.playlist

import dev.olog.core.entity.id.AuthorIdentifier
import dev.olog.core.entity.id.CollectionIdentifier
import dev.olog.core.entity.id.PlayableIdentifier
import dev.olog.core.entity.id.PlaylistIdentifier
import dev.olog.core.author.Artist
import dev.olog.core.playlist.Playlist
import dev.olog.core.playable.Song
import dev.olog.data.playlist.playlists.SelectRelatedArtists
import org.junit.Assert
import org.junit.Test

class PlaylistMapperTest {

    @Test
    fun `test Playlists_view toDomain`() {
        val actual = Playlists_view(
            id = 1,
            title = "title",
            songs = 2,
            path = "path"
        ).toDomain()

        val expected = Playlist(
            id = PlaylistIdentifier.MediaStore(1, false),
            title = "title",
            size = 2,
            path = "path",
        )

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test Podcast_playlists_view toDomain`() {
        val actual = Podcast_playlists_view(
            id = 1,
            title = "title",
            songs = 2,
            path = "path"
        ).toDomain()

        val expected = Playlist(
            id = PlaylistIdentifier.MediaStore(1, true),
            title = "title",
            size = 2,
            path = "path",
        )

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test SelectRelatedArtists toDomain`() {
        val actual = SelectRelatedArtists(
            author_id = 1,
            author = "author",
            songs = 2,
        ).toDomain()

        val expected = Artist(
            id = AuthorIdentifier.MediaStore(1, false),
            name = "author",
            songs = 2,
        )

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test Playlists_playables_view toDomain`() {
        val actual = Playlists_playables_view(
            id = 1,
            author_id = 2,
            collection_id = 3,
            title = "title",
            author = "author",
            album_artist = "album_artist",
            collection = "collection",
            duration = 10,
            date_added = 20,
            directory = "directory",
            path = "path",
            disc_number = 100,
            track_number = 200,
            is_podcast = true,
            playlist_id = 300,
            play_order = 400
        ).toDomain()

        val expected = Song(
            id = PlayableIdentifier.MediaStore(1, true),
            artistId = AuthorIdentifier.MediaStore(2, true),
            albumId = CollectionIdentifier.MediaStore(3, true),
            title = "title",
            artist = "author",
            albumArtist = "album_artist",
            album = "collection",
            duration = 10,
            dateAdded = 20,
            directory = "directory",
            path = "path",
            discNumber = 100,
            trackNumber = 200,
            idInPlaylist = 400,
        )

        Assert.assertEquals(expected, actual)
    }

}
package dev.olog.data.sort

import org.junit.Assert
import org.junit.Test

class SortTableTest {

    @Test
    fun `test serialized value`() {
        val map = mapOf(
            SortTable.Folders to "folders",
            SortTable.Playlists to "playlists",
            SortTable.Songs to "songs",
            SortTable.Artists to "artists",
            SortTable.Albums to "albums",
            SortTable.Genres to "genres",
            SortTable.PodcastPlaylists to "podcast_playlists",
            SortTable.PodcastEpisodes to "podcast_episodes",
            SortTable.PodcastAuthors to "podcast_authors",
            SortTable.PodcastCollections to "podcast_collections",
            SortTable.FoldersSongs to "folders_songs",
            SortTable.PlaylistsSongs to "playlists_songs",
            SortTable.ArtistsSongs to "artists_songs",
            SortTable.AlbumsSongs to "albums_songs",
            SortTable.GenresSongs to "genres_songs",
            SortTable.PodcastPlaylistsEpisodes to "podcast_playlists_episodes",
            SortTable.PodcastAuthorsEpisodes to "podcast_authors_episodes",
            SortTable.PodcastCollectionsEpisodes to "podcast_collections_episodes",
        )

        for (item in SortTable.values()) {
            Assert.assertEquals(map[item], item.serialized)
        }
    }

}
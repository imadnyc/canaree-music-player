package dev.olog.data.author

import dev.olog.testing.ArtistView
import dev.olog.testing.IndexedTrack
import dev.olog.data.QueriesConstants
import dev.olog.data.QueriesConstants.recentlyAddedMaxTime
import dev.olog.data.TestDatabase
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.days

class ArtistQueriesTest {

    private val db = TestDatabase()
    private val indexedQueries = db.indexedPlayablesQueries
    private val blacklistQueries = db.blacklistQueries
    private val queries = db.artistsQueries

    @Before
    fun setup() {
        blacklistQueries.insert("dir")

        indexedQueries.insert(IndexedTrack(id = "100", author_id = "10", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "101", author_id = "10", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "102", author_id = "10", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "103", author_id = "20", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "104", author_id = "20", is_podcast = false))
        // below should be filtered
        indexedQueries.insert(IndexedTrack(id = "105", author_id = "30", is_podcast = true))
        indexedQueries.insert(IndexedTrack(id = "106", author_id = "30", is_podcast = false, directory = "dir"))
    }

    @Test
    fun `test selectAll`() {
        val actual = queries.selectAll().executeAsList()
        val expected = listOf(
            ArtistView(id = "10", songs = 3),
            ArtistView(id = "20", songs = 2),
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test selectById`() {
        val actual = queries.selectById("10").executeAsOne()
        val expected = ArtistView(id = "10", songs = 3)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test selectById, should be null when not present`() {
        val actual = queries.selectById("50").executeAsOneOrNull()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun `test selectRecentlyAdded`() {
        var id = 0L
        fun song(authorId: Long, dateAdded: Long, directory: String = "") = IndexedTrack(
            id = (++id).toString(),
            author_id = authorId.toString(),
            date_added = dateAdded,
            is_podcast = false,
            directory = directory
        )

        indexedQueries.deleteAll()

        val unixTimestamp = QueriesConstants.unitTimestamp()
        indexedQueries.insert(song(authorId = 10, dateAdded = unixTimestamp))
        indexedQueries.insert(song(authorId = 20, dateAdded = unixTimestamp - recentlyAddedMaxTime + 1.days.inWholeSeconds))
        indexedQueries.insert(song(authorId = 30, dateAdded = unixTimestamp - recentlyAddedMaxTime + 3.days.inWholeSeconds))
        indexedQueries.insert(song(authorId = 40, dateAdded = unixTimestamp - recentlyAddedMaxTime + 2.days.inWholeSeconds))
        // below should be skipped
        indexedQueries.insert(song(authorId = 50, dateAdded = unixTimestamp - recentlyAddedMaxTime - 1.days.inWholeSeconds))
        indexedQueries.insert(song(authorId = 60, dateAdded = unixTimestamp, directory = "dir"))

        val actual = queries.selectRecentlyAdded().executeAsList()
        val expected = listOf(
            ArtistView(id = "10", songs = 1, dateAdded = unixTimestamp),
            ArtistView(id = "30", songs = 1, dateAdded = unixTimestamp - recentlyAddedMaxTime + 3.days.inWholeSeconds),
            ArtistView(id = "40", songs = 1, dateAdded = unixTimestamp - recentlyAddedMaxTime + 2.days.inWholeSeconds),
            ArtistView(id = "20", songs = 1, dateAdded = unixTimestamp - recentlyAddedMaxTime + 1.days.inWholeSeconds),
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `test lastPlayed, max 10 items`() {
        indexedQueries.insert(IndexedTrack(id = "1", author_id = "1", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "2", author_id = "2", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "3", author_id = "3", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "4", author_id = "4", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "5", author_id = "5", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "6", author_id = "6", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "7", author_id = "7", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "8", author_id = "8", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "9", author_id = "9", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "10", author_id = "10", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "11", author_id = "11", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "12", author_id = "12", is_podcast = false))
        indexedQueries.insert(IndexedTrack(id = "13", author_id = "12", is_podcast = false))

        queries.insertRecentlyPlayed(id = "1", date_played = 10)
        queries.insertRecentlyPlayed(id = "2", date_played = 20)
        queries.insertRecentlyPlayed(id = "3", date_played = 30)
        queries.insertRecentlyPlayed(id = "4", date_played = 40)
        queries.insertRecentlyPlayed(id = "5", date_played = 50)
        queries.insertRecentlyPlayed(id = "6", date_played = 60)
        queries.insertRecentlyPlayed(id = "7", date_played = 70)
        queries.insertRecentlyPlayed(id = "8", date_played = 100)
        queries.insertRecentlyPlayed(id = "9", date_played = 80)
        queries.insertRecentlyPlayed(id = "10", date_played = 5)
        queries.insertRecentlyPlayed(id = "11", date_played = 2)
        queries.insertRecentlyPlayed(id = "12", date_played = 90)

        val actual = queries.selectRecentlyPlayed().executeAsList()
        val expected = listOf(
            ArtistView(id = "8", songs = 1),
            ArtistView(id = "12", songs = 2),
            ArtistView(id = "9", songs = 1),
            ArtistView(id = "7", songs = 1),
            ArtistView(id = "6", songs = 1),
            ArtistView(id = "5", songs = 1),
            ArtistView(id = "4", songs = 1),
            ArtistView(id = "3", songs = 1),
            ArtistView(id = "2", songs = 1),
            ArtistView(id = "1", songs = 1),
        )
        Assert.assertEquals(expected, actual)
    }

}
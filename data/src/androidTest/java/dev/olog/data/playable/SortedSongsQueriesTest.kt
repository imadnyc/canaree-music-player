package dev.olog.data.playable

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.olog.core.entity.sort.SortArranging
import dev.olog.core.entity.sort.SortType
import dev.olog.data.AndroidIndexedPlayables
import dev.olog.data.AndroidTestDatabase
import dev.olog.data.Blacklist
import dev.olog.data.insertGroup
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SortedSongsQueriesTest {

    private val db = AndroidTestDatabase()
    private val indexedQueries = db.indexedPlayablesQueries
    private val blacklistQueries = db.blacklistQueries
    private val sortQueries = db.sortQueries
    private val queries = db.songsQueries

    @Before
    fun setup() {
        blacklistQueries.insert(Blacklist("yes"))
        // item to be filtered, blacklisted and podcast
        indexedQueries.insert(AndroidIndexedPlayables(id = 1000, is_podcast = false, directory = "yes"))
        indexedQueries.insert(AndroidIndexedPlayables(id = 1001, is_podcast = true, directory = "no"))
        indexedQueries.insert(AndroidIndexedPlayables(id = 1002, is_podcast = true, directory = "yes"))

        // insert data
        val data = listOf(
            AndroidIndexedPlayables(
                id = 1,
                title = "àtitle",
                author = "azz",
                collection = "íttt",
                is_podcast = false,
                duration = 20,
                date_added = 15
            ),
            AndroidIndexedPlayables(
                id = 2,
                title = "âspace",
                author = "äaa",
                collection = "ïhello",
                is_podcast = false,
                duration = 15,
                date_added = 40,
            ),
            AndroidIndexedPlayables(
                id = 3,
                title = "êtitle",
                author = "<unknown>",
                collection = "<unknown>",
                is_podcast = false,
                duration = 50,
                date_added = 15,
            ),
            AndroidIndexedPlayables(
                id = 4,
                title = "ėspace",
                author = "<unknown>",
                collection = "<unknown>",
                is_podcast = false,
                duration = 50,
                date_added = 40
            ),
            AndroidIndexedPlayables(
                id = 5,
                title = "zzz",
                author = "zee",
                collection = "def",
                is_podcast = false,
                duration = 50,
                date_added = 25
            ),
            AndroidIndexedPlayables(
                id = 6,
                title = "random",
                author = "zee",
                collection = "def",
                is_podcast = false,
                duration = 50,
                date_added = 40
            ),
        )
        indexedQueries.insertGroup(data)
    }

    @Test
    fun testObserveAllSortedByTitleAscending() {
        // ignore accents
        val expected = listOf(
            "âspace",
            "àtitle",
            "ėspace",
            "êtitle",
            "random",
            "zzz",
        )

        // when ascending
        sortQueries.replaceSongsSort(SortType.TITLE, SortArranging.ASCENDING)
        val actualAsc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected, actualAsc.map { it.title })

        // when descending
        sortQueries.replaceSongsSort(SortType.TITLE, SortArranging.DESCENDING)
        val actualDesc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected.reversed(), actualDesc.map { it.title })
    }

    @Test
    fun testObserveAllSortedByAuthorAscending() {
        // when ascending
        val expectedAsc = listOf(
            // ignore accents
            "äaa" to "âspace",
            "azz" to "àtitle",
            // second sort on title when same author
            "zee" to "random",
            "zee" to "zzz",
            // <unknown> always last, second sort on title when same author
            "<unknown>" to "ėspace",
            "<unknown>" to "êtitle",
        )

        sortQueries.replaceSongsSort(SortType.ARTIST, SortArranging.ASCENDING)
        val actualAsc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expectedAsc, actualAsc.map { it.author to it.title })

        // when descending
        val expectedDesc = listOf(
            // second sort on title when same author
            "zee" to "zzz",
            "zee" to "random",
            // ignore accents
            "azz" to "àtitle",
            "äaa" to "âspace",
            // <unknown> always last, second sort on title when same author
            "<unknown>" to "êtitle",
            "<unknown>" to "ėspace",
        )

        sortQueries.replaceSongsSort(SortType.ARTIST, SortArranging.DESCENDING)
        val actualDesc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expectedDesc, actualDesc.map { it.author to it.title })
    }

    @Test
    fun testObserveAllSortedByCollectionAscending() {
        // when ascending
        val expectedAsc = listOf(
            // second sort on title when same collection
            "def" to "random",
            "def" to "zzz",
            // ignore accents
            "ïhello" to "âspace",
            "íttt" to "àtitle",
            // <unknown> always last, second sort on title when same author
            "<unknown>" to "ėspace",
            "<unknown>" to "êtitle",
        )
        sortQueries.replaceSongsSort(SortType.ALBUM, SortArranging.ASCENDING)
        val actualAsc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expectedAsc, actualAsc.map { it.collection to it.title })

        // when descending
        val expectedDesc = listOf(
            // ignore accents
            "íttt" to "àtitle",
            "ïhello" to "âspace",
            // second sort on title when same collection
            "def" to "zzz",
            "def" to "random",
            // <unknown> always last, second sort on title when same author
            "<unknown>" to "êtitle",
            "<unknown>" to "ėspace",
        )
        sortQueries.replaceSongsSort(SortType.ALBUM, SortArranging.DESCENDING)
        val actualDesc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expectedDesc, actualDesc.map { it.collection to it.title })
    }

    @Test
    fun testObserveAllSortedByDurationAscending() {
        // when ascending
        val expected = listOf(
            15L to "âspace",
            20L to "àtitle",
            // second sort on title when same duration
            50L to "ėspace",
            50L to "êtitle",
            50L to "random",
            50L to "zzz",
        )
        sortQueries.replaceSongsSort(SortType.DURATION, SortArranging.ASCENDING)
        val actualAsc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected, actualAsc.map { it.duration to it.title })

        // when descending
        sortQueries.replaceSongsSort(SortType.DURATION, SortArranging.DESCENDING)
        val actualDesc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected.reversed(), actualDesc.map { it.duration to it.title })
    }

    @Test
    fun testObserveAllSortedByRecentlyAddedAscending() {
        // when ascending
        val expected = listOf(
            // descending, second sort title ascending
            40L to "âspace",
            40L to "ėspace",
            40L to "random",
            25L to "zzz",
            15L to "àtitle",
            15L to "êtitle",
        )
        sortQueries.replaceSongsSort(SortType.RECENTLY_ADDED, SortArranging.ASCENDING)
        val actualAsc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected, actualAsc.map { it.date_added to it.title })

        // when descending
        sortQueries.replaceSongsSort(SortType.RECENTLY_ADDED, SortArranging.DESCENDING)
        val actualDesc = queries.selectAllSorted().executeAsList()
        Assert.assertEquals(expected.reversed(), actualDesc.map { it.date_added to it.title })
    }


    // TODO test flow updates on blacklist and sort

}
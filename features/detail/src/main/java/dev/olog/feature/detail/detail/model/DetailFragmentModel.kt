package dev.olog.feature.detail.detail.model

import androidx.annotation.LayoutRes
import dev.olog.core.MediaId
import dev.olog.feature.detail.R
import dev.olog.presentation.model.BaseModel

internal sealed class DetailFragmentModel(
    @LayoutRes open val layoutType: Int
) : BaseModel {

    val isTrack: Boolean
        get() = this is Track || this is PlaylistTrack

    override val type: Int
        get() = TODO("Not yet implemented")
    override val mediaId: MediaId
        get() = TODO("Not yet implemented")

    data class MainHeader(
        override val mediaId: MediaId,
        val title: String,
        val subtitle: String
    ) : DetailFragmentModel(R.layout.item_detail_image)

    data class Biography(
        val content: String
    ) : DetailFragmentModel(R.layout.item_detail_biography)

    data class Track(
        private val layoutRes: Int,
        override val mediaId: MediaId,
        val title: String,
        val subtitle: String,
        val trackNumber: Int,
    ) : DetailFragmentModel(layoutRes)

    data class PlaylistTrack(
        private val layoutRes: Int,
        override val mediaId: MediaId,
        val title: String,
        val subtitle: String,
        val idInPlaylist: Int,
    ) : DetailFragmentModel(layoutRes)

    // TODO visible = false
    data class MostPlayedHeader(
        val title: String,
    ) : DetailFragmentModel(R.layout.item_detail_header)

    object MostPlayedList : DetailFragmentModel(R.layout.item_detail_list_most_played)

    data class RelatedArtistHeader(
        val title: String,
        val showSeeAll: Boolean,
    ) : DetailFragmentModel(R.layout.item_detail_header)

    object RelatedArtistList : DetailFragmentModel(R.layout.item_detail_list_related_artists)

    data class RecentlyAddedHeader(
        val title: String,
        val subtitle: String,
        val showSeeAll: Boolean,
    ) : DetailFragmentModel(R.layout.item_detail_header_recently_added)

    object RecentlyAddedList : DetailFragmentModel(R.layout.item_detail_list_recently_added)

    data class AlbumsHeader(
        val title: String
    ) : DetailFragmentModel(R.layout.item_detail_header_albums)

    object AlbumsList : DetailFragmentModel(R.layout.item_detail_list_albums)

    object Shuffle : DetailFragmentModel(R.layout.item_detail_shuffle)

    data class AllTracksHeader(
        val title: String,
        val subtitle: String
    ) : DetailFragmentModel(R.layout.item_detail_header_all_song)

    object EmptyState : DetailFragmentModel(R.layout.item_detail_empty_state)

    data class Duration(
        val content: String
    ) : DetailFragmentModel(R.layout.item_detail_song_footer)

}
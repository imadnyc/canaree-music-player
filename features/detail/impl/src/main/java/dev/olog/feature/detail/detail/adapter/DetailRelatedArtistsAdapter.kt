package dev.olog.feature.detail.detail.adapter

import android.view.View
import androidx.lifecycle.Lifecycle
import dev.olog.core.MediaId
import dev.olog.feature.base.BindingsAdapter
import dev.olog.feature.base.adapter.*
import dev.olog.feature.base.model.DisplayableAlbum
import dev.olog.feature.base.model.DisplayableItem
import kotlinx.android.synthetic.main.item_detail_related_artist.view.*

class DetailRelatedArtistsAdapter(
    lifecycle: Lifecycle,
    private val onItemClick: (MediaId) -> Unit,
    private val onItemLongClick: (MediaId, View) -> Unit,
) : ObservableAdapter<DisplayableItem>(
    lifecycle,
    DiffCallbackDisplayableItem
) {

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        viewHolder.setOnClickListener(this) { item, _, _ ->
            onItemClick(item.mediaId)
        }
        viewHolder.setOnLongClickListener(this) { item, _, _ ->
            onItemLongClick(item.mediaId, viewHolder.itemView)
        }
        viewHolder.elevateAlbumOnTouch()
    }

    override fun bind(holder: DataBoundViewHolder, item: DisplayableItem, position: Int) {
        require(item is DisplayableAlbum)

        holder.itemView.apply {
            BindingsAdapter.loadAlbumImage(holder.imageView!!, item.mediaId)
            firstText.text = item.title
            secondText.text = item.subtitle
            quickAction.setId(item.mediaId)
        }
    }
}
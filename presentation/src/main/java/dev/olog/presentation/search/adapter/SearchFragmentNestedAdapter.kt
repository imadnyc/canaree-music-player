package dev.olog.presentation.search.adapter

import dev.olog.lib.image.provider.ImageLoader
import dev.olog.presentation.base.adapter.*
import dev.olog.presentation.model.DisplayableAlbum
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.navigator.NavigatorLegacy
import dev.olog.presentation.search.SearchFragmentViewModel
import kotlinx.android.synthetic.main.item_search_album.*

class SearchFragmentNestedAdapter(
    private val navigator: NavigatorLegacy,
    private val viewModel: SearchFragmentViewModel
) : ObservableAdapter<DisplayableItem>(DiffCallbackDisplayableItem) {

    override fun initViewHolderListeners(viewHolder: LayoutContainerViewHolder, viewType: Int) {
        viewHolder.setOnClickListener(this) { item, _, _ ->
            navigator.toDetailFragment(item.mediaId)
            viewModel.insertToRecent(item.mediaId)
        }
        viewHolder.setOnLongClickListener(this) { item, _, _ ->
            navigator.toDialog(item.mediaId, viewHolder.itemView)
        }
        viewHolder.elevateAlbumOnTouch()
    }

    override fun bind(
        holder: LayoutContainerViewHolder,
        item: DisplayableItem,
        position: Int
    ) = holder.bindView {
        require(item is DisplayableAlbum)

        ImageLoader.loadAlbumImage(imageView!!, item.mediaId)
        quickAction.setId(item.mediaId)
        firstText.text = item.title
        secondText?.text = item.subtitle
    }

}
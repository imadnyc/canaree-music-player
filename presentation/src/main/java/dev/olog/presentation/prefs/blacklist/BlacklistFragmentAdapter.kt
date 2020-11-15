package dev.olog.presentation.prefs.blacklist

import dev.olog.presentation.BindingsAdapter
import dev.olog.presentation.base.adapter.LayoutContainerViewHolder
import dev.olog.presentation.base.adapter.SimpleAdapter
import dev.olog.shared.android.extensions.toggleVisibility
import kotlinx.android.synthetic.main.dialog_blacklist_item.*

class BlacklistFragmentAdapter(
    data: List<BlacklistModel>
) : SimpleAdapter<BlacklistModel>(data.toMutableList()) {

    override fun getItemViewType(position: Int): Int = dataSet[position].type

    override fun initViewHolderListeners(viewHolder: LayoutContainerViewHolder, viewType: Int) {
        viewHolder.itemView.setOnClickListener {
            getItem(viewHolder.adapterPosition)?.let { item ->
                item.isBlacklisted = !item.isBlacklisted
                notifyItemChanged(viewHolder.adapterPosition)
            }
        }
    }

    override fun LayoutContainerViewHolder.bind(
        item: BlacklistModel,
        position: Int
    ) = bindView {
        BindingsAdapter.loadAlbumImage(imageView!!, item.mediaId)
        scrim.toggleVisibility(item.isBlacklisted, true)
        firstText.text = item.title
        secondText.text = item.displayablePath
    }

}
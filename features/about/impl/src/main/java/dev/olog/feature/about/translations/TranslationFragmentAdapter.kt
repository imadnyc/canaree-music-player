package dev.olog.feature.about.translations

import dev.olog.feature.about.R
import dev.olog.feature.about.navigator.NavigatorAbout
import dev.olog.feature.base.adapter.DataBoundViewHolder
import dev.olog.feature.base.adapter.SimpleAdapter
import kotlinx.android.synthetic.main.item_translations_contributor.view.*

class TranslationFragmentAdapter(
    data: MutableList<String>,
    private val navigator: NavigatorAbout
) : SimpleAdapter<String>(data) {

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        if (viewType == R.layout.item_translations_help){
            viewHolder.itemView.setOnClickListener {
                navigator.requestTranslation()
            }
        }
    }

    override fun bind(holder: DataBoundViewHolder, item: String, position: Int) {
        if (holder.itemViewType == R.layout.item_translations_contributor) {
            holder.itemView.text.text = item
        }
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> R.layout.item_translations_help
        1 -> R.layout.item_translations_header
        else -> R.layout.item_translations_contributor
    }
}
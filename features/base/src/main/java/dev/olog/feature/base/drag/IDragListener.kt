package dev.olog.feature.base.drag

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


interface IDragListener {
    var itemTouchHelper: ItemTouchHelper?

    fun setupDragListener(
        list: RecyclerView,
        adapter: TouchableAdapter,
        direction: Int
    )

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}

class DragListenerImpl : IDragListener {
    override var itemTouchHelper: ItemTouchHelper? = null

    override fun setupDragListener(
        list: RecyclerView,
        adapter: TouchableAdapter,
        direction: Int
    ) {
        val callback = TouchHelperAdapterCallback(adapter, direction)
        itemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(list)
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }
}
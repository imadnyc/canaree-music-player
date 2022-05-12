package dev.olog.feature.library.tab.manager

import androidx.recyclerview.widget.GridLayoutManager
import dev.olog.feature.library.tab.SpanCountController

abstract class AbsSpanSizeLookup(
    var requestedSpanSize: Int
) : GridLayoutManager.SpanSizeLookup() {

    fun getSpanCount() = SpanCountController.SPAN_COUNT

}
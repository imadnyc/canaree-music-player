package dev.olog.feature.lyrics.offline

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import dev.olog.shared.extension.findInContext
import dev.olog.ui.extension.tint

object LyricsOfflineTutorial {

    fun addLyrics(search: View, edit: View, sync: View){
        val context = search.context

        val searchTarget = TapTarget.forView(search, context.getString(localization.R.string.tutorial_search_lyrics))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, dev.olog.ui.R.drawable.vd_search))

        val editTarget = TapTarget.forView(edit, context.getString(localization.R.string.tutorial_add_lyrics))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, dev.olog.ui.R.drawable.vd_edit))

        val syncLyrics = TapTarget.forView(sync, context.getString(localization.R.string.tutorial_adjust_sync))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, dev.olog.ui.R.drawable.vd_sync))

        TapTargetSequence(search.context.findInContext<Activity>())
                .targets(editTarget, searchTarget, syncLyrics)
                .start()
    }

}
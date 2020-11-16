package dev.olog.presentation.popup

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Menu
import android.view.View
import dev.olog.core.entity.track.Playlist
import dev.olog.presentation.R
import me.saket.cascade.CascadePopupMenu

abstract class AbsPopup(
    view: View
) : CascadePopupMenu(view.context, view) {

    companion object {
        const val NEW_PLAYLIST_ID = Int.MIN_VALUE
    }

    fun addPlaylistChooser(context: Context, playlists: List<Playlist>){
        val addToPlaylistMenuItem = menu.findItem(R.id.addToPlaylist)
        val addToPlaylistSubMenu = addToPlaylistMenuItem.subMenu

        playlists.forEach { addToPlaylistSubMenu.add(Menu.NONE, it.id.toInt(), Menu.NONE, it.title) }
        val spannableString = SpannableString("${context.getString(R.string.popup_new_playlist)}..")
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, spannableString.length, 0)
        addToPlaylistSubMenu.add(Menu.NONE,
            NEW_PLAYLIST_ID, Menu.NONE, spannableString)

    }

}
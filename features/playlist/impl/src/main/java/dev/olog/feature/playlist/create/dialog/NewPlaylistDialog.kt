package dev.olog.feature.playlist.create.dialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.core.MediaId
import dev.olog.feature.base.BaseEditTextDialog
import dev.olog.shared.android.extensions.act
import dev.olog.shared.android.extensions.toast
import dev.olog.shared.android.extensions.withArguments
import dev.olog.shared.lazyFast
import javax.inject.Inject

@AndroidEntryPoint
class NewPlaylistDialog : BaseEditTextDialog() {

    companion object {
        const val TAG = "NewPlaylistDialog"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"
        const val ARGUMENTS_LIST_SIZE = "$TAG.arguments.list_size"
        const val ARGUMENTS_ITEM_TITLE = "$TAG.arguments.item_title"

        @JvmStatic
        fun newInstance(mediaId: MediaId, listSize: Int, itemTitle: String): NewPlaylistDialog {
            return NewPlaylistDialog().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId.toString(),
                    ARGUMENTS_LIST_SIZE to listSize,
                    ARGUMENTS_ITEM_TITLE to itemTitle
            )
        }
    }

    @Inject lateinit var presenter: NewPlaylistDialogPresenter

    private val mediaId: MediaId by lazyFast {
        val mediaId = arguments!!.getString(ARGUMENTS_MEDIA_ID)!!
        MediaId.fromString(mediaId)
    }
    private val title: String by lazyFast { arguments!!.getString(ARGUMENTS_ITEM_TITLE)!! }
    private val listSize: Int by lazyFast { arguments!!.getInt(ARGUMENTS_LIST_SIZE) }

    override fun extendBuilder(builder: MaterialAlertDialogBuilder): MaterialAlertDialogBuilder {
        return super.extendBuilder(builder)
            .setTitle(localization.R.string.popup_new_playlist)
            .setPositiveButton(localization.R.string.popup_positive_create, null)
            .setNegativeButton(localization.R.string.popup_negative_cancel, null)
    }

    override fun setupEditText(layout: TextInputLayout, editText: TextInputEditText) {
        editText.hint = getString(localization.R.string.popup_new_playlist)
    }

    override fun provideMessageForBlank(): String {
        return getString(localization.R.string.popup_playlist_name_not_valid)
    }

    override suspend fun onItemValid(string: String) {
        var message: String
        try {
            presenter.execute(mediaId, string)
            message = successMessage(act, string).toString()
        } catch (ex: Throwable) {
            ex.printStackTrace()
            message = getString(localization.R.string.popup_error_message)
        }
        act.toast(message)
    }


    private fun successMessage(context: Context, currentValue: String): CharSequence {
        if (mediaId.isPlayingQueue){
            return context.getString(localization.R.string.queue_saved_as_playlist, currentValue)
        }
        if (mediaId.isLeaf){
            return context.getString(localization.R.string.added_song_x_to_playlist_y, title, currentValue)
        }
        return context.resources.getQuantityString(localization.R.plurals.xx_songs_added_to_playlist_y,
                listSize, listSize, currentValue)
    }
}
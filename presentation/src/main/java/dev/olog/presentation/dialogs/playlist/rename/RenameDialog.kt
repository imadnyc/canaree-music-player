package dev.olog.presentation.dialogs.playlist.rename

import android.content.Context
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.core.MediaId
import dev.olog.presentation.R
import dev.olog.presentation.dialogs.BaseEditTextDialog
import dev.olog.shared.extension.argument
import dev.olog.shared.extension.toast
import dev.olog.shared.extension.withArguments

@AndroidEntryPoint
class RenameDialog : BaseEditTextDialog() {

    companion object {
        const val TAG = "DeleteDialog"
        const val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"
        const val ARGUMENTS_ITEM_TITLE = "$TAG.arguments.item_title"

        @JvmStatic
        fun newInstance(mediaId: MediaId, itemTitle: String): RenameDialog {
            return RenameDialog().withArguments(
                    ARGUMENTS_MEDIA_ID to mediaId,
                    ARGUMENTS_ITEM_TITLE to itemTitle
            )
        }
    }

    private val viewModel by viewModels<RenameDialogViewModel>()

    private val mediaId by argument<MediaId>(ARGUMENTS_MEDIA_ID)
    private val itemTitle by argument<String>(ARGUMENTS_ITEM_TITLE)

    override fun extendBuilder(builder: MaterialAlertDialogBuilder): MaterialAlertDialogBuilder {
        return super.extendBuilder(builder)
            .setTitle(R.string.popup_rename)
            .setPositiveButton(R.string.popup_positive_rename, null)
            .setNegativeButton(R.string.popup_negative_cancel, null)
    }

    override fun setupEditText(layout: TextInputLayout, editText: TextInputEditText) {
        editText.setText(arguments!!.getString(ARGUMENTS_ITEM_TITLE))
    }

    override fun provideMessageForBlank(): String {
        return when {
            mediaId.isPlaylist || mediaId.isPodcastPlaylist -> getString(R.string.popup_playlist_name_not_valid)
            else -> throw IllegalArgumentException("invalid media id category $mediaId")
        }
    }

    override suspend fun onItemValid(string: String) {
        var message: String
        try {
            viewModel.execute(mediaId, string)
            message = successMessage(requireContext(), string)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            message = getString(R.string.popup_error_message)
        }
        toast(message)
    }

    private fun successMessage(context: Context, currentValue: String): String {
        return when {
            mediaId.isPlaylist || mediaId.isPodcastPlaylist -> context.getString(R.string.playlist_x_renamed_to_y, itemTitle, currentValue)
            else -> throw IllegalStateException("not a playlist, $mediaId")
        }
    }
}
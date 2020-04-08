package dev.olog.presentation.prefs.blacklist

import android.os.Environment
import dev.olog.domain.entity.track.Folder
import dev.olog.domain.gateway.track.FolderGateway
import dev.olog.domain.prefs.BlacklistPreferences
import dev.olog.feature.presentation.base.model.PresentationId
import dev.olog.presentation.R
import dev.olog.feature.presentation.base.model.BaseModel
import dev.olog.feature.presentation.base.model.presentationId
import dev.olog.shared.lazyFast
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BlacklistFragmentPresenter @Inject constructor(
    folderGateway: FolderGateway,
    private val appPreferencesUseCase: BlacklistPreferences
) {

    val data : List<BlacklistModel> by lazyFast {
        val blacklisted = appPreferencesUseCase.getBlackList().map { it.toLowerCase(Locale.getDefault()) }
        folderGateway.getAllBlacklistedIncluded().map { it.toDisplayableItem(blacklisted) }
    }

    private fun Folder.toDisplayableItem(blacklisted: List<String>): BlacklistModel {
        return BlacklistModel(
            R.layout.dialog_blacklist_item,
            presentationId,
            this.title,
            this.path,
            blacklisted.contains(this.path.toLowerCase(Locale.getDefault()))
        )
    }

    fun saveBlacklisted(data: List<BlacklistModel>) {
        val blacklisted = data.filter { it.isBlacklisted }
            .map { it.path }
            .toSet()
        appPreferencesUseCase.setBlackList(blacklisted)
    }


}

data class BlacklistModel(
    override val type: Int,
    override val mediaId: PresentationId.Category,
    val title: String,
    val path: String,
    var isBlacklisted: Boolean
) : BaseModel {

    companion object {
        @Suppress("DEPRECATION")
        @JvmStatic
        private val defaultStorageDir = Environment.getExternalStorageDirectory().path ?: "/storage/emulated/0/"
    }

    // show the path without "/storage/emulated/0"
    val displayablePath : String
        get() {
            return try {
                path.substring(defaultStorageDir.length)
            } catch (ex: StringIndexOutOfBoundsException){
                Timber.e(ex)
                path
            }
        }

}
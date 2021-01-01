package dev.olog.domain.interactor

import dev.olog.domain.mediaid.MediaId
import dev.olog.domain.mediaid.MediaIdCategory
import dev.olog.domain.entity.track.Track
import dev.olog.domain.gateway.track.FolderGateway
import dev.olog.domain.gateway.track.GenreGateway
import dev.olog.domain.interactor.base.FlowUseCaseWithParam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveRecentlyAddedUseCase @Inject constructor(
    private val folderGateway: FolderGateway,
    private val genreGateway: GenreGateway

) : FlowUseCaseWithParam<List<Track>, MediaId>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun buildUseCase(mediaId: MediaId): Flow<List<Track>> {
        return when (mediaId.category){
            MediaIdCategory.FOLDERS -> folderGateway.observeRecentlyAdded(mediaId.categoryValue)
            MediaIdCategory.GENRES -> genreGateway.observeRecentlyAdded(mediaId.categoryId)
            else -> flowOf(listOf())
        }
    }
}
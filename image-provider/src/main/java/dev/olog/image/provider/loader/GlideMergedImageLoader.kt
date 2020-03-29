package dev.olog.image.provider.loader

import android.content.Context
import android.net.Uri
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import dev.olog.core.MediaId
import dev.olog.core.MediaIdCategory.*
import dev.olog.core.gateway.track.FolderGateway
import dev.olog.core.gateway.track.GenreGateway
import dev.olog.core.gateway.track.PlaylistGateway
import dev.olog.core.prefs.AppPreferencesGateway
import dev.olog.image.provider.fetcher.GlideMergedImageFetcher
import dev.olog.shared.ApplicationContext
import java.io.InputStream
import javax.inject.Inject

private val allowedCategories = listOf(
    FOLDERS, PLAYLISTS, GENRES, PODCASTS_PLAYLIST
)

class GlideMergedImageLoader(
    private val context: Context,
    private val uriLoader: ModelLoader<Uri, InputStream>,
    private val folderGateway: FolderGateway,
    private val playlistGateway: PlaylistGateway,
    private val genreGateway: GenreGateway,
    private val prefsGateway: AppPreferencesGateway
) : ModelLoader<MediaId.Category, InputStream> {

    override fun handles(mediaId: MediaId.Category): Boolean {
        return mediaId.category in allowedCategories
    }

    override fun buildLoadData(
        mediaId: MediaId.Category,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        if (!prefsGateway.canAutoCreateImages()) {
//             skip
            return uriLoader.buildLoadData(Uri.EMPTY, width, height, options)
        }

        return ModelLoader.LoadData(
            MediaIdKey(mediaId),
            GlideMergedImageFetcher(
                context,
                mediaId,
                folderGateway,
                playlistGateway,
                genreGateway
            )
        )
    }

    class Factory @Inject constructor(
        @ApplicationContext private val context: Context,
        private val folderGateway: FolderGateway,
        private val playlistGateway: PlaylistGateway,
        private val genreGateway: GenreGateway,
        private val prefsGateway: AppPreferencesGateway
    ) : ModelLoaderFactory<MediaId.Category, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<MediaId.Category, InputStream> {
            val uriLoader = multiFactory.build(Uri::class.java, InputStream::class.java)
            return GlideMergedImageLoader(
                context,
                uriLoader,
                folderGateway,
                playlistGateway,
                genreGateway,
                prefsGateway
            )
        }

        override fun teardown() {

        }
    }
}


package dev.olog.lib.image.provider.fetcher

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import dev.olog.core.mediaid.MediaId
import dev.olog.core.entity.track.Track
import dev.olog.core.gateway.podcast.PodcastGateway
import dev.olog.core.gateway.track.SongGateway
import dev.olog.lib.image.provider.executor.GlideScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.InputStream

class GlideOriginalImageFetcher(
    private val context: Context,
    private val mediaId: MediaId,
    private val songGateway: SongGateway,
    private val podcastGateway: PodcastGateway

) : DataFetcher<InputStream> {

    private val scope: CoroutineScope = GlideScope()

    override fun getDataClass(): Class<InputStream> = InputStream::class.java
    override fun getDataSource(): DataSource = DataSource.LOCAL

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        scope.launch {
            val id = getId()
            if (id == -1L) {
                callback.onLoadFailed(Exception("item not found for id$id"))
                return@launch
            }

            val track: Track? = when {
                mediaId.isAlbum -> songGateway.getByAlbumId(id)
                mediaId.isPodcastAlbum -> podcastGateway.getByAlbumId(id)
                mediaId.isLeaf && !mediaId.isPodcast -> songGateway.getByParam(id)
                mediaId.isLeaf && mediaId.isPodcast -> podcastGateway.getByParam(id)
                else -> {
                    callback.onLoadFailed(IllegalArgumentException("not a valid media id=$mediaId"))
                    return@launch
                }
            }
            yield()

            if (track == null) {
                callback.onLoadFailed(IllegalArgumentException("track not found for id $id"))
                return@launch
            }
            try {
                val stream = OriginalImageFetcher.loadImage(context, track)
                callback.onDataReady(stream)
            } catch (ex: Throwable) {
                callback.onLoadFailed(RuntimeException(ex))
            }
        }
    }



    private fun getId(): Long {
        if (mediaId.isAlbum || mediaId.isPodcastAlbum){
            return mediaId.categoryId
        }
        if (mediaId.isLeaf){
            return mediaId.leaf!!
        }
        return -1
    }

    override fun cleanup() {

    }

    override fun cancel() {
        scope.cancel()
    }

}
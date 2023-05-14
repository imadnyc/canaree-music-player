package dev.olog.image.provider.fetcher

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import dev.olog.core.MediaId
import dev.olog.core.MediaIdCategory
import dev.olog.core.gateway.track.FolderGateway
import dev.olog.core.gateway.track.GenreGateway
import dev.olog.core.gateway.track.PlaylistGateway
import dev.olog.image.provider.creator.ImagesFolderUtils
import dev.olog.image.provider.creator.MergedImagesCreator
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.lang.RuntimeException

class GlideMergedImageFetcher(
    private val context: Context,
    private val mediaId: MediaId,
    private val folderGateway: FolderGateway,
    private val playlistGateway: PlaylistGateway,
    private val genreGateway: GenreGateway
) : DataFetcher<InputStream> {

    override fun loadData(
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) = runBlocking {
        try {
            val inputStream = when (mediaId.category) {
                MediaIdCategory.FOLDERS -> makeFolderImage(mediaId.id)
                MediaIdCategory.GENRES -> makeGenreImage(mediaId.id)
                MediaIdCategory.PLAYLISTS -> makePlaylistImage(mediaId)
                else -> error("$mediaId not supported")
            }
            callback.onDataReady(inputStream)
        } catch (ex: Throwable){
            callback.onLoadFailed(RuntimeException(ex))
        }
    }


    private suspend fun makeFolderImage(folderId: Long): InputStream? {
//        val folderImage = ImagesFolderUtils.forFolder(context, dirPath) --contains current image
        val albumsId = folderGateway.getTrackListById(folderId).map { it.albumId }

        val folderName = ImagesFolderUtils.FOLDER

        val file = MergedImagesCreator.makeImages(
            context = context,
            albumIdList = albumsId,
            parentFolder = folderName,
            itemId = "$folderId"
        )
        return file?.inputStream()
    }

    private suspend fun makeGenreImage(genreId: Long): InputStream? {
//        ImagesFolderUtils.forGenre(context, id) --contains current image

        val albumsId = genreGateway.getTrackListById(genreId).map { it.albumId }

        val folderName = ImagesFolderUtils.GENRE
        val file = MergedImagesCreator.makeImages(
            context = context,
            albumIdList = albumsId,
            parentFolder = folderName,
            itemId = "$genreId"
        )
        return file?.inputStream()
    }

    private suspend fun makePlaylistImage(mediaId: MediaId): InputStream? {
        val playlistId = mediaId.id

//        ImagesFolderUtils.forPlaylist(context, id) --contains current image
        val albumsId = playlistGateway.getTrackListById(mediaId).map { it.albumId }

        val folderName = ImagesFolderUtils.PLAYLIST
        val file = MergedImagesCreator.makeImages(
            context = context,
            albumIdList = albumsId,
            parentFolder = folderName,
            itemId = "$playlistId"
        )
        return file?.inputStream()
    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.LOCAL

    override fun cleanup() {

    }

    override fun cancel() {

    }

}
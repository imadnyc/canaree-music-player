package dev.olog.msc.utils.k.extension

import android.support.v4.media.MediaMetadataCompat
import dev.olog.core.MediaId
import dev.olog.msc.constants.MusicConstants
import dev.olog.presentation.model.DisplayableItem
import dev.olog.shared.TextUtils

fun MediaMetadataCompat.getTitle(): CharSequence {
    return getText(MediaMetadataCompat.METADATA_KEY_TITLE)
}

fun MediaMetadataCompat.getArtist(): CharSequence {
    val artist = getText(MediaMetadataCompat.METADATA_KEY_ARTIST)
    return DisplayableItem.adjustArtist(artist.toString())
}

fun MediaMetadataCompat.getAlbum(): CharSequence {
    val album = getText(MediaMetadataCompat.METADATA_KEY_ALBUM)
    return DisplayableItem.adjustAlbum(album.toString())
}

fun MediaMetadataCompat.getDuration(): Long {
    return getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
}

fun MediaMetadataCompat.getDurationReadable(): String {
    val duration = getDuration()
    return TextUtils.formatMillis(duration)
}

fun MediaMetadataCompat.getMediaId(): MediaId {
    val mediaId = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
    return MediaId.fromString(mediaId)
}

fun MediaMetadataCompat.getId(): Long {
    return getMediaId().leaf!!
}

fun MediaMetadataCompat.isPodcast(): Boolean {
    return getLong(MusicConstants.IS_PODCAST) != 0L
}
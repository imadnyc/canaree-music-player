package dev.olog.presentation.model

import dev.olog.domain.entity.*
import dev.olog.presentation.R
import dev.olog.shared.MediaIdHelper
import dev.olog.shared.TextUtils

fun Folder.toDisplayableItem(): DisplayableItem{
    return DisplayableItem(
            R.layout.item_tab_album,
            MediaIdHelper.folderId(path),
            title.capitalize()
    )
}

fun Playlist.toDisplayableItem(): DisplayableItem{
    return dev.olog.presentation.model.DisplayableItem(
            R.layout.item_tab_album,
            MediaIdHelper.playlistId(id),
            title.capitalize()
    )
}

fun Song.toDisplayableItem(): DisplayableItem{
    return DisplayableItem(
            R.layout.item_tab_song,
            MediaIdHelper.songId(id),
            title,
            "$artist${TextUtils.MIDDLE_DOT_SPACED}$album",
            image,
            true,
            isRemix,
            isExplicit
    )
}

fun Album.toDisplayableItem(): DisplayableItem{
    return DisplayableItem(
            R.layout.item_tab_album,
            MediaIdHelper.albumId(id),
            title,
            artist,
            image
    )
}

fun Artist.toDisplayableItem(): DisplayableItem{
    return DisplayableItem(
            R.layout.item_tab_album,
            MediaIdHelper.artistId(id),
            name
    )
}

fun Genre.toDisplayableItem(): DisplayableItem{
    return DisplayableItem(
            R.layout.item_tab_album,
            MediaIdHelper.genreId(id),
            name
    )
}
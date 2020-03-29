@file:Suppress("UNCHECKED_CAST")

package dev.olog.presentation.detail

import dev.olog.presentation.model.DisplayableAlbum
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.model.DisplayableTrack

class DetailValues (
    val songs: List<DisplayableItem>,
    mostPlayed: List<DisplayableItem>,
    recentlyAdded: List<DisplayableItem>,
    relatedArtists: List<DisplayableItem>,
    siblings: List<DisplayableItem>,
    spotifyAppearsOn: List<DisplayableItem>,
    spotifyAlbums: List<DisplayableItem>
) {

    val mostPlayed: List<DisplayableTrack> = mostPlayed as List<DisplayableTrack>
    val recentlyAdded: List<DisplayableTrack> = recentlyAdded as List<DisplayableTrack>
    val relatedArtists: List<DisplayableAlbum> = relatedArtists as List<DisplayableAlbum>
    val siblings: List<DisplayableAlbum> = siblings as List<DisplayableAlbum>
    val spotifySingles: List<DisplayableAlbum> = spotifyAppearsOn as List<DisplayableAlbum>
    val spotifyAlbums: List<DisplayableAlbum> = spotifyAlbums as List<DisplayableAlbum>

}
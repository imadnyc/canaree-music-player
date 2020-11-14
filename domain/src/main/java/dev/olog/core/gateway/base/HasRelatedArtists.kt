package dev.olog.core.gateway.base

import dev.olog.core.entity.track.Artist
import kotlinx.coroutines.flow.Flow

interface HasRelatedArtists<Param> {
    fun observeRelatedArtists(params: Param): Flow<List<Artist>>
}
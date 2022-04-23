package dev.olog.presentation.player

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.olog.platform.theme.hasPlayerAppearance
import dev.olog.presentation.model.PresentationPreferencesGateway
import dev.olog.ui.adaptive.InvalidPaletteColors
import dev.olog.ui.adaptive.InvalidProcessColors
import dev.olog.ui.adaptive.PaletteColors
import dev.olog.ui.adaptive.ProcessorColors
import dev.olog.ui.adaptive.ValidPaletteColors
import dev.olog.ui.adaptive.ValidProcessorColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class PlayerFragmentPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val presentationPrefs: PresentationPreferencesGateway
) {

    private val processorPublisher = ConflatedBroadcastChannel<ProcessorColors>()
    private val palettePublisher = ConflatedBroadcastChannel<PaletteColors>()

    fun observePlayerControlsVisibility(): Flow<Boolean> {
        return presentationPrefs.observePlayerControlsVisibility()
    }

    // allow adaptive color on flat appearance
    fun observeProcessorColors(): Flow<ProcessorColors> {

        return processorPublisher.asFlow()
            .map {
                val hasPlayerAppearance = context.hasPlayerAppearance()
                if (presentationPrefs.isAdaptiveColorEnabled() || hasPlayerAppearance.isFlat()) {
                    it
                } else {
                    InvalidProcessColors
                }
            }
            .filter { it is ValidProcessorColors }
            .flowOn(Dispatchers.Default)
    }

    // allow adaptive color on flat appearance
    fun observePaletteColors(): Flow<PaletteColors> {

        return palettePublisher
            .asFlow()
            .map {
                val hasPlayerAppearance = context.hasPlayerAppearance()
                if (presentationPrefs.isAdaptiveColorEnabled() || hasPlayerAppearance.isFlat() || hasPlayerAppearance.isSpotify()) {
                    it
                } else {
                    InvalidPaletteColors
                }
            }
            .filter { it is ValidPaletteColors }
            .flowOn(Dispatchers.Default)
    }

    fun updateProcessorColors(palette: ProcessorColors) {
        processorPublisher.trySend(palette)
    }

    fun updatePaletteColors(palette: PaletteColors) {
        palettePublisher.trySend(palette)
    }


}
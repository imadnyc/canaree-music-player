package dev.olog.msc.app

import androidx.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import dev.olog.appshortcuts.AppShortcuts
import dev.olog.core.AppInitializer
import dev.olog.feature.media.api.SleepTimerUseCase
import dev.olog.msc.R
import io.alterac.blurkit.BlurKit
import javax.inject.Inject

@HiltAndroidApp
class App : ThemedApp() {

    private lateinit var appShortcuts: AppShortcuts


    @Inject
    lateinit var sleepTimerUseCase: SleepTimerUseCase

    @Inject
    lateinit var initializers: Set<@JvmSuppressWildcards AppInitializer>


    override fun onCreate() {
        super.onCreate()
        initializers.forEach { it.initialize() }
        initializeComponents()
        initializeConstants()
        resetSleepTimer()

        registerActivityLifecycleCallbacks(CustomTabsActivityLifecycleCallback())
    }

    private fun initializeComponents() {
        appShortcuts = AppShortcuts.instance(this)

        BlurKit.init(this)
    }

    private fun initializeConstants() {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
    }

    private fun resetSleepTimer() {
        sleepTimerUseCase.reset()
    }

}

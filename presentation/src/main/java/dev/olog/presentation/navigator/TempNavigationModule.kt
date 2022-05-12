package dev.olog.presentation.navigator

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dev.olog.platform.BottomNavigationFragmentTag
import dev.olog.feature.library.LibraryFragment
import dev.olog.feature.queue.PlayingQueueFragment
import dev.olog.feature.search.SearchFragment

@Module
@InstallIn(SingletonComponent::class)
class TempNavigationModule {

    @Provides
    @IntoSet
    fun provideLibraryTrackTag(): BottomNavigationFragmentTag {
        return BottomNavigationFragmentTag { LibraryFragment.TAG_TRACK }
    }

    @Provides
    @IntoSet
    fun provideLibraryPodcastTag(): BottomNavigationFragmentTag {
        return BottomNavigationFragmentTag { LibraryFragment.TAG_PODCAST }
    }

    @Provides
    @IntoSet
    fun provideSearchTag(): BottomNavigationFragmentTag {
        return BottomNavigationFragmentTag { SearchFragment.TAG }
    }

    @Provides
    @IntoSet
    fun provideQueueTag(): BottomNavigationFragmentTag {
        return BottomNavigationFragmentTag { PlayingQueueFragment.TAG }
    }

}
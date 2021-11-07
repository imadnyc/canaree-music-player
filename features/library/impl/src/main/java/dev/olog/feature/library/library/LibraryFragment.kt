package dev.olog.feature.library.library

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.core.MediaIdCategory
import dev.olog.feature.base.BaseFragment
import dev.olog.feature.dialogs.FeatureDialogsNavigator
import dev.olog.feature.floating.FeatureFloatingNavigator
import dev.olog.feature.library.LibraryPage
import dev.olog.feature.library.LibraryPrefs
import dev.olog.feature.library.R
import dev.olog.feature.main.BottomNavigationPage
import dev.olog.feature.main.HasBottomNavigation
import dev.olog.shared.android.extensions.*
import dev.olog.shared.lazyFast
import dev.olog.shared.widgets.TutorialTapTarget
import kotlinx.android.synthetic.main.fragment_library.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LibraryFragment : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG_TRACK = LibraryFragment::class.java.name
        @JvmStatic
        val TAG_PODCAST = LibraryFragment::class.java.name + ".podcast"
        const val IS_PODCAST = "IS_PODCAST"

        @JvmStatic
        fun newInstance(isPodcast: Boolean): LibraryFragment {
            return LibraryFragment().withArguments(
                IS_PODCAST to isPodcast
            )
        }
    }

    @Inject
    lateinit var presenter: LibraryFragmentPresenter
    @Inject
    lateinit var libraryPrefs: LibraryPrefs
    @Inject
    lateinit var dialogNavigator: FeatureDialogsNavigator
    @Inject
    lateinit var floatingNavigator: FeatureFloatingNavigator

    private val isPodcast by lazyFast {
        getArgument<Boolean>(
            IS_PODCAST
        )
    }

    private val pagerAdapter by lazyFast {
        LibraryFragmentAdapter(
            context = act.applicationContext,
            fragmentManager = childFragmentManager,
            categories = presenter.getCategories(isPodcast),
            folderTreePref = libraryPrefs.useFolderTree,
        )
    }

    fun isCurrentFragmentFolderTree(): Boolean {
        return pagerAdapter.getCategoryAtPosition(viewPager.currentItem) == MediaIdCategory.FOLDERS &&
                pagerAdapter.showFolderAsHierarchy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null){
            val transaction = childFragmentManager.beginTransaction()
            for (fragment in childFragmentManager.fragments) {
                transaction.remove(fragment)
            }
            transaction.commitNowAllowingStateLoss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.currentItem = presenter.getViewPagerLastPage(pagerAdapter.count, isPodcast)
        viewPager.offscreenPageLimit = 5

        pagerEmptyState.toggleVisibility(pagerAdapter.isEmpty(), true)

        val selectedView: TextView = if (!isPodcast) tracks else podcasts
        val unselectedView: TextView = if (!isPodcast) podcasts else tracks
        selectedView.setTextColor(requireContext().textColorPrimary())
        unselectedView.setTextColor(requireContext().textColorSecondary())

        if (!presenter.canShowPodcasts()){
            podcasts.setGone()
        }

        if (presenter.showFloatingWindowTutorialIfNeverShown()) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(500)
                TutorialTapTarget.floatingWindow(floatingWindow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewPager.addOnPageChangeListener(onPageChangeListener)
        more.setOnClickListener { dialogNavigator.toMainPopup(requireActivity(), it, createMediaId()) }
        floatingWindow.setOnClickListener { startServiceOrRequestOverlayPermission() }

        tracks.setOnClickListener { changeLibraryPage(LibraryPage.TRACKS) }
        podcasts.setOnClickListener { changeLibraryPage(LibraryPage.PODCASTS) }
    }

    override fun onPause() {
        super.onPause()
        viewPager.removeOnPageChangeListener(onPageChangeListener)
        more.setOnClickListener(null)
        floatingWindow.setOnClickListener(null)
        tracks.setOnClickListener(null)
        podcasts.setOnClickListener(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.adapter = null
    }

    private fun changeLibraryPage(page: LibraryPage) {
        presenter.setLibraryPage(page)
        (requireActivity() as HasBottomNavigation).navigate(BottomNavigationPage.LIBRARY)
    }

    private fun createMediaId(): MediaIdCategory? {
        return pagerAdapter.getCategoryAtPosition(viewPager.currentItem)
    }

    private fun startServiceOrRequestOverlayPermission() {
        floatingNavigator.startService(requireActivity())
    }

    private val onPageChangeListener =
        object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                presenter.setViewPagerLastPage(position, isPodcast)
            }
        }

    override fun provideLayoutId(): Int = R.layout.fragment_library
}
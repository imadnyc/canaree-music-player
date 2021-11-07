package dev.olog.feature.detail.related.artist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.core.MediaId
import dev.olog.feature.base.BaseFragment
import dev.olog.feature.detail.FeatureDetailNavigator
import dev.olog.feature.detail.R
import dev.olog.feature.dialogs.FeatureDialogsNavigator
import dev.olog.scrollhelper.layoutmanagers.OverScrollGridLayoutManager
import dev.olog.shared.android.extensions.act
import dev.olog.shared.android.extensions.subscribe
import dev.olog.shared.android.extensions.withArguments
import dev.olog.shared.lazyFast
import kotlinx.android.synthetic.main.fragment_related_artist.*
import javax.inject.Inject

@AndroidEntryPoint
class RelatedArtistFragment : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG = RelatedArtistFragment::class.java.name
        @JvmStatic
        val ARGUMENTS_MEDIA_ID = "$TAG.arguments.media_id"

        @JvmStatic
        fun newInstance(mediaId: MediaId): RelatedArtistFragment {
            return RelatedArtistFragment().withArguments(
                ARGUMENTS_MEDIA_ID to mediaId.toString()
            )
        }
    }

    @Inject
    lateinit var detailNavigator: FeatureDetailNavigator
    @Inject
    lateinit var dialogNavigator: FeatureDialogsNavigator

    private val adapter by lazyFast {
        RelatedArtistFragmentAdapter(
            lifecycle = lifecycle,
            onItemClick = { detailNavigator.toDetailFragment(requireActivity(), it) },
            onItemLongClick = { mediaId, view -> dialogNavigator.toDialog(requireActivity(), mediaId, view) }
        )
    }

    private val viewModel by viewModels<RelatedArtistFragmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list.layoutManager = OverScrollGridLayoutManager(list, 2)
        list.adapter = adapter
        list.setHasFixedSize(true)

        viewModel.observeData()
            .subscribe(viewLifecycleOwner, adapter::updateDataSet)

        viewModel.observeTitle()
            .subscribe(viewLifecycleOwner) { itemTitle ->
                val headersArray = resources.getStringArray(localization.R.array.related_artists_header)
                val header = String.format(headersArray[viewModel.itemOrdinal], itemTitle)
                this.header.text = header
            }
    }

    override fun onResume() {
        super.onResume()
        back.setOnClickListener { act.onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        back.setOnClickListener(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        list.adapter = null
    }

    override fun provideLayoutId(): Int = R.layout.fragment_related_artist
}
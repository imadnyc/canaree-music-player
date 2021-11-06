package dev.olog.feature.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.feature.about.navigator.NavigatorAbout
import dev.olog.feature.base.BaseFragment
import dev.olog.scrollhelper.layoutmanagers.OverScrollLinearLayoutManager
import dev.olog.shared.android.extensions.act
import dev.olog.shared.android.extensions.subscribe
import dev.olog.shared.lazyFast
import kotlinx.android.synthetic.main.fragment_about.*
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG = AboutFragment::class.java.name
    }

    @Inject
    lateinit var navigator: NavigatorAbout

    private val presenter by viewModels<AboutFragmentPresenter>()
    private val adapter by lazyFast {
        AboutFragmentAdapter(lifecycle, navigator)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list.layoutManager = OverScrollLinearLayoutManager(list)
        list.adapter = adapter

        presenter.observeData()
            .subscribe(viewLifecycleOwner, adapter::updateDataSet)
    }

    override fun onResume() {
        super.onResume()
        back.setOnClickListener { act.onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        back.setOnClickListener(null)
    }

    override fun provideLayoutId(): Int = R.layout.fragment_about
}
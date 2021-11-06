package dev.olog.feature.playlist.choose

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.olog.feature.base.BaseActivity
import dev.olog.feature.playlist.R
import dev.olog.shared.android.extensions.subscribe
import dev.olog.shared.android.extensions.toast
import dev.olog.shared.lazyFast
import kotlinx.android.synthetic.main.activity_playlist_chooser.*

@AndroidEntryPoint
class PlaylistChooserActivity : BaseActivity() {

    private val viewModel by viewModels<PlaylistChooserActivityViewModel>()

    private val adapter by lazyFast { PlaylistChooserActivityAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_chooser)

        viewModel.observeData()
            .subscribe(this) { list ->
                if (list.isEmpty()){
                    toast("No playlist found")
                    finish()
                } else {
                    adapter.updateDataSet(list)
                }
            }

        list.adapter = adapter
        list.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onResume() {
        super.onResume()
        back.setOnClickListener { finish() }
    }

    override fun onPause() {
        super.onPause()
        back.setOnClickListener(null)
    }

}
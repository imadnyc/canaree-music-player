package dev.olog.feature.splash

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dev.olog.compose.CanareeIcons
import dev.olog.compose.components.CanareeBackground
import dev.olog.compose.components.CanareeFab
import dev.olog.compose.components.CanareePagerIndicator
import dev.olog.shared.extension.exhaustive
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onRequestPermission: suspend () -> Unit,
) {
    CanareeBackground(
        modifier = Modifier.fillMaxSize(),
    ) {
        val state = rememberPagerState(initialPage = 0)

        CanareePagerIndicator(
            pagerState = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
        )

        HorizontalPager(count = 2, state = state) { page ->
            when (page) {
                0 -> SplashPresentation()
                1 -> SplashTutorial(
                    disallowParentInterceptEvent = {
                        // todo disable scroll
                    }
                )
                else -> error("invalid page $page")
            }.exhaustive
        }

        NextFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            when (state.currentPage) {
                0 -> state.animateScrollToPage(1)
                1 -> onRequestPermission()
                else -> error("invalid page ${state.currentPage}")
            }.exhaustive
        }
    }
}

@Composable
private fun NextFab(
    modifier: Modifier = Modifier,
    onClick: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()

    CanareeFab(
        imageVector = CanareeIcons.KeyboardArrowRight,
        modifier = modifier,
        onClick = {
            scope.launch { onClick() }
        }
    )
}
package dev.olog.msc.presentation.utils.images

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.widget.ImageView
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import dev.olog.msc.R
import dev.olog.msc.presentation.widget.ForegroundImageView
import dev.olog.msc.presentation.widget.parallax.ParallaxImageView
import dev.olog.msc.utils.ViewUtils
import dev.olog.msc.utils.k.extension.getBitmap

class RippleTarget(
        private val imageView: ImageView,
        private val isLeaf : Boolean

) : DrawableImageViewTarget(imageView), Palette.PaletteAsyncListener {

    init {
        if (isLeaf && imageView is ForegroundImageView){
            imageView.foreground = null
        }
    }

    override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {
        super.onResourceReady(drawable, transition)
        if (!isLeaf && imageView is ForegroundImageView){
            val bitmap = drawable.getBitmap() ?: return
            Palette.from(bitmap).clearFilters().generate(this)
        }
    }

    override fun onGenerated(palette: Palette) {
        if (!isLeaf && imageView is ForegroundImageView){
            val fallbackColor = ContextCompat.getColor(view.context, R.color.mid_grey)
            val darkAlpha = .5f
            val lightAlpha = .6f

            imageView.foreground = ViewUtils.createRipple(palette, darkAlpha,
                    lightAlpha, fallbackColor, true)

            if (imageView is ParallaxImageView){
                imageView.setScrimColor(ViewUtils.createRippleColor(palette, darkAlpha,
                        lightAlpha, fallbackColor))
            }
        }
    }
}
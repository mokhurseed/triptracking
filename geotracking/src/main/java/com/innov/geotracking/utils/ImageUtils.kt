package com.innov.geotracking.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.innov.geotracking.R
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy

class ImageUtils {

    companion object {
        var INSTANCE: ImageUtils? = null
        fun setImageInstance() {
            if (INSTANCE == null) {
                INSTANCE = ImageUtils()
            }
        }
    }

    fun loadLocalGIFImage(imageView: ImageView, image: Int) {
        try {
            imageView.run {
                Glide.with(context)
                    .asGif()
                    .load(image)
                    .override(width, height)
                    .placeholder(R.drawable.loader)
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(this)
            }
        } catch (e: Exception) {
        }
    }
}
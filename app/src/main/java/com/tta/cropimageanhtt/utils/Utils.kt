package com.tta.cropimageanhtt.utils

import android.content.res.Resources

object Utils {

    const val ACTION_CROP_IMAGE = "ACTION_CROP_IMAGE"
    const val ACTION_CROP_IMAGE_FROM_CAMERA = "ACTION_CROP_IMAGE_FROM_CAMERA"
    const val ACTION_CROP_IMAGE_FROM_GALLERY = "ACTION_CROP_IMAGE_FROM_GALLERY"

    const val TIME_DELAY_CLICK_IN_MILISECONDS: Long = 500

    const val ALPHA_ENABLE = 1f
    const val ALPHA_DISABLE = 0.4f

    const val FOLDER_CACHE_CROP_IMAGE = "crop_image"
    const val CROP_PATH = "CROP_PATH"
    const val AVATAR_FOLDER = "avatar"

    @JvmStatic
    fun dpToPx(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }
}
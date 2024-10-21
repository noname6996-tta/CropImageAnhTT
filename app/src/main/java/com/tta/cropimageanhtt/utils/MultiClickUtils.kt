package com.tta.cropimageanhtt.utils

import android.os.SystemClock
import com.tta.cropimageanhtt.utils.Utils.TIME_DELAY_CLICK_IN_MILISECONDS

class MultiClickUtils private constructor() {

    private var mLastClickTime: Long
    val isAvailableClick: Boolean
        get() {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - mLastClickTime < TIME_DELAY_CLICK_IN_MILISECONDS) {
//                Timber.d("Can not apply click!")
                return false
            }
            mLastClickTime = currentTime
//            Timber.d("Apply click!")
            return true
        }

    fun isAvailableClick(timeDelay: Long): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - mLastClickTime < timeDelay) {
//            Timber.d("Can not apply click!")
            return false
        }
        mLastClickTime = currentTime
//        Timber.d("Apply click!")
        return true
    }

    companion object {
        var instance: MultiClickUtils? = null
            get() {
                if (field == null) field = MultiClickUtils()
                return field
            }
            private set
    }

    init {
        mLastClickTime = -1
    }
}
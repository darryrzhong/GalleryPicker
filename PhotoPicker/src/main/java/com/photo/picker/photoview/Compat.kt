package com.photo.picker.photoview

import android.annotation.TargetApi
import android.os.Build
import android.view.View

internal object Compat {
    fun postOnAnimation(view: View, runnable: Runnable) {
        postOnAnimationJellyBean(view, runnable)
    }

    @TargetApi(16)
    private fun postOnAnimationJellyBean(view: View, runnable: Runnable) {
        view.postOnAnimation(runnable)
    }
}

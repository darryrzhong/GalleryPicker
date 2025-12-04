package com.photo.picker.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver

/**
 * <pre>
 *     类描述  : Activity生命周期回调
 *
 *
 *     @author : never
 *     @since   : 2024/10/11
 * </pre>
 */
internal object LifecycleObserverHelper {

    private var mLifecycleObserver: DefaultLifecycleObserver? = null

    fun bindObserver(observer: DefaultLifecycleObserver) {
        mLifecycleObserver = observer
    }

    fun removeObserver() {
        mLifecycleObserver = null
    }

    fun getObserver(): DefaultLifecycleObserver? {
        return mLifecycleObserver
    }

}
package com.photo.picker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.photo.picker.lifecycle.LifecycleObserverHelper

/**
 * <pre>
 *     类描述  : 空处理页面
 *
 *
 *     @author : never
 *     @since   : 2024/10/10
 * </pre>
 */
class EmptyHandleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val mLifecycleObserver = LifecycleObserverHelper.getObserver()
        if (mLifecycleObserver != null) {
            lifecycle.addObserver(mLifecycleObserver)
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }


}
package com.photo.picker.utils

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/11
 * </pre>
 */

fun View.getColorFromRes(res: Int): Int {
    return ContextCompat.getColor(context, res)
}
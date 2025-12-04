package com.gallery.picker

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.widget.Toast
import kotlin.math.roundToInt

/**
 * <pre>
 *     类描述  :
 *
 *     @author : never
 *     @since  : 2025/12/2
 * </pre>
 */
/**
 * @receiver dp 2 px
 */
val Float.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).roundToInt()

/**
 * @receiver sp 2 px
 */
//val Int.sp: Float
//    get() = (this * Resources.getSystem().displayMetrics.scaledDensity + 0.5f)

/**
 * @receiver sp 2 px
 */
val Float.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

fun String.showTips(context: Context){
    Toast(context).apply {
        setText(this@showTips)
        duration = Toast.LENGTH_SHORT
        show()
    }
}

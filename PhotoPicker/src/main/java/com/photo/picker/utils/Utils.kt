package com.photo.picker.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import com.photo.picker.GalleryPickerOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/8
 * </pre>
 */
object Utils {

    const val TAG = "PhotoLog"

    /**
     * 宽高比例
     *
     * @param widthProportion
     * @param heightProportion
     * @param width
     * @return
     */
    fun getHeight(widthProportion: Int, heightProportion: Int, width: Int): Int {
        val temp = width / widthProportion
        return temp * heightProportion
    }

    fun createDrawable(context: Context, color: Int, radiusDp: Float): Drawable {
        // 创建 GradientDrawable 对象
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE // 设置形状为矩形
            cornerRadius = TypedValue.applyDimension( // 设置圆角半径，dp 转为像素
                TypedValue.COMPLEX_UNIT_DIP,
                radiusDp,
                context.resources.displayMetrics
            )
            setColor(color) // 设置背景颜色
        }
        return drawable
    }

    suspend fun createCameraFile(context: Context): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "picture_photo").also { it.mkdirs() }
        val timeStamp = System.currentTimeMillis().toString()
        val imageFile = File(cacheDir, "$timeStamp.jpg")

        // 创建文件
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        imageFile
    }

    suspend fun createCaptureVideoFile(context: Context): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "picture_photo").also { it.mkdirs() }
        val timeStamp = System.currentTimeMillis().toString()
        val imageFile = File(cacheDir, "$timeStamp.mp4")

        // 创建文件
        if (!imageFile.exists()) {
            imageFile.createNewFile()
        }
        imageFile
    }

    fun showToast(context: Context, resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun log(msg: String) {
        if (GalleryPickerOption.debug) {
            Log.d(TAG, msg)
        }
    }
}
package com.photo.picker.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream


/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/8
 * </pre>
 */
object BitmapUtils {

    /**
     * 图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
     * @param filePath 图片路径
     */
    fun decodeFile(filePath: String) = flow<Bitmap?> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        //先获取一下宽高
        try {
            BitmapFactory.decodeFile(filePath, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        val minSize = 1080
        val (width, height) = options.outWidth to options.outHeight
        val requiredSize = minSize.coerceAtMost(width.coerceAtMost(height))

        if (requiredSize == 0) {
            emit(null)
            return@flow
        }
        // 计算缩放比例
        val scale = (1.coerceAtLeast(width / requiredSize)).coerceAtMost(height / requiredSize)

        // 设置采样比例
        options.apply {
            inSampleSize = scale
            inPreferredConfig = Bitmap.Config.RGB_565
            inJustDecodeBounds = false
        }

        var bitmap: Bitmap? = null
        // 尝试解码图片
        try {
            bitmap = BitmapFactory.decodeFile(filePath, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        if (bitmap == null) {
            emit(null)
            return@flow
        }

        val degree = readPictureDegree(filePath)
        try {
            if (degree != 0) {
                val bit = rotateImageView(degree, bitmap)
                emit(bit)
            } else {
                emit(bitmap)
            }
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            emit(bitmap)
        }
    }.flowOn(Dispatchers.IO)


    /**
     * 读取图片的旋转角度
     * @param path 图片路径
     */
    private fun readPictureDegree(path: String): Int {
        return try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 旋转图片
     * @param degree 旋转角度
     * @param bitmap 原图片
     */
    private fun rotateImageView(degree: Int, bitmap: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degree.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun saveBitmapInDisCache(filename: String, bitmap: Bitmap) = flow<String> {
        val bytes: ByteArray? = compressImageClassPhoto(bitmap)
        bytes?.let {
            writeImageToDisk(filename, bytes)
            emit(filename)
        }
    }

    private fun compressImageClassPhoto(image: Bitmap): ByteArray? {
        val baos = ByteArrayOutputStream()
        val quality = 75
        return try {
            image.compress(
                Bitmap.CompressFormat.JPEG, quality, baos
            ) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            baos.close()
        }

    }

    /**
     * 将图片写入到磁盘
     *
     * @param img 图片数据流
     */
    private fun writeImageToDisk(filepath: String?, img: ByteArray?) {
        val fops = FileOutputStream(filepath)
        try {
            fops.write(img)
            fops.flush()
            fops.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            fops.close()
        }
    }

}
@file:Suppress("MemberVisibilityCanBePrivate")

package com.photo.picker.luban

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

/**
 * 鲁班图片压缩
 *
 * @author never
 * @date 2024/09/29
 */
class Luban(private val context: Context) {

    companion object {
        fun sizeOf(file: File): Size {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            options.inSampleSize = 1

            BitmapFactory.decodeFile(file.absolutePath, options)
            return Size(options.outWidth, options.outHeight)
        }

        fun kbOf(file: File): Long {
            return file.length() shr 10
        }
    }

    internal val providers = mutableListOf<InputStreamProvider>()

    /** 文件输出路径 */
    internal var outputDir: File? = null
    internal var focusAlpha: Boolean = false
    internal var keepSize: Boolean = false
    internal var quality: Int = 75
    internal var ignoreSize: Int = 100
    internal var filter: ((String) -> Boolean)? = null
    internal var renamer: ((String) -> File)? = null

    /** 缓存目录 */
    private val cacheDir: File by lazy {
        val name = "luban"
        val dir = context.cacheDir.absolutePath?.let { f -> File(f, name).also { it.mkdirs() } }
        if (dir?.exists() == true) return@lazy dir
        File(context.cacheDir, name).also { it.mkdirs() }
    }

    /**
     * 流类型
     */
    fun load(provider: InputStreamProvider) = apply { providers.add(provider) }

    /**
     * 文件类型
     */
    fun load(file: File) = apply { providers.add(FileInputStreamProvider(file)) }

    /**
     * 文件路径类型
     */
    fun load(path: String) = apply { load(File(path)) }

    /**
     * 文件路径类型
     */
    fun load(uri: Uri) = apply { providers.add(UriInputStreamProvider(context, uri)) }

    /**
     * 批量处理，支持类型: File or FilePath, Uri.
     */
    fun <T> load(files: List<T>) = apply {
        files.forEach {
            when (it) {
                is String -> load(it)
                is File -> load(it)
                is Uri -> load(it)
                else -> throw IllegalStateException("Only supports there types: File, String, Uri")
            }
        }
    }

    /** 压缩图片比例，实际是传入[Bitmap.compress]方法的quality参数，默认值60% */
    fun quality(value: Int) = apply { this.quality = value }

    /** 保存图片原始比例，但压缩效果会降低，默认false */
    fun keepSize(value: Boolean) = apply { this.keepSize = value }

    /**
     * 压缩输出目录，如果为空则默认为再缓存目录下。
     */
    fun outputDir(dir: File) = apply { outputDir = dir }

    /**
     * 是否保留透明通道，该参数只支持png图片，非png图片设置无效。
     * - true: 保留alpha通道，压缩速度会很慢
     * - false:  不保留alpha通道，它可能有黑色背景。
     */
    fun keepAlpha(value: Boolean) = apply { this.focusAlpha = value }

    /**
     * 当原始图像文件大小小于这个值时不进行压缩，单位为KB。
     */
    fun ignoreSize(value: Int) = apply { this.ignoreSize = value }

    /** 过滤器，当前满足过滤条件的文件会被压缩，默认请返回true.  */
    fun filter(block: (String) -> Boolean) = apply { this.filter = block }


    /**
     * 运行在IO线程中,批量处理，再返回处理。
     */
    suspend fun launch(): List<CompressResult> {
        val result = mutableListOf<CompressResult>()
        launchInternal {
            if (it is CompressResult.Success || it is CompressResult.Error) {
                result.add(it)
            }
        }
        providers.clear()
        renamer = null
        filter = null
        return result
    }

    /**
     * 运行在IO线程中，逐个处理回调。
     */
    suspend fun collect(emit: (CompressResult) -> Unit) {
        launchInternal(emit)
    }

    /**
     * 只获取一个压缩结果，如果有多个将抛出异常。
     */
    suspend fun single(): CompressResult = launch().single()

    /**
     * 真正的处理方法
     */
    private suspend fun launchInternal(emit: (CompressResult) -> Unit) {
        if (providers.isEmpty()) {
            return emit(CompressResult.Error(null, NullPointerException("No files to compress")))
        }
        val total = providers.size
        providers.forEachIndexed { index, provider ->
            val path = provider.path
            if (path.isNullOrEmpty()) {
                emit(CompressResult.Error(null, NullPointerException("the path at $index is null")))
                return@forEachIndexed
            }
            // 过滤器
            if (filter != null && !filter!!.invoke(path)) {
                debug("this compass file is filtered: $path")
                emit(CompressResult.Success(File(path), index, total))
                return@forEachIndexed
            }
            // 检查是否需要压缩
            if (!Checker.SINGLE.needCompress(ignoreSize, path)) {
                emit(CompressResult.Success(File(path), index, total))
                return@forEachIndexed
            }
            runCatching {
                emit(CompressResult.Start(provider.path, index, total))
                val file = withContext(Dispatchers.IO) { compass(provider) }
                emit(CompressResult.Success(file, index, total))
            }.onFailure { e ->
                withContext(Dispatchers.Main) { emit(CompressResult.Error(provider.path, e)) }
            }
            provider.close()
        }
    }

    /**
     * 调用[Engine]进行压缩
     */
    private fun compass(provider: InputStreamProvider): File {
        val checker = Checker.SINGLE
        val path = provider.path!!
        val suffix = checker.extSuffix(provider)
        val output = renamer?.invoke(path) ?: makeCacheTempFile(suffix)
        return Engine(provider, output, focusAlpha, quality, keepSize).compress()
    }

    /**
     * 创建临时文件
     */
    private fun makeCacheTempFile(suffix: String): File {
        var root = cacheDir
        outputDir?.also { if (it.exists()) root = it }
        val random = Random.nextInt(1000)
        val fileName = "luban_cache_${System.currentTimeMillis()}${random}$suffix"
        return File(root, fileName)
    }

    private fun debug(msg: String) {
        Log.d("luban", msg)
    }
}
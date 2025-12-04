package com.photo.picker

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.photo.picker.callback.MediaResultCallback
import com.photo.picker.crop.ClipPhotoActivity
import com.photo.picker.utils.Utils
import com.photo.picker.lifecycle.LifecycleObserverHelper
import com.photo.picker.luban.CompressResult
import com.photo.picker.luban.Luban
import com.photo.picker.ui.EmptyHandleActivity
import com.photo.picker.ui.ImagePreViewActivity
import com.photo.picker.ui.VideoPreViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

/**
 * <pre>
 *     类描述  : 照片选择器帮助类
 *
 *
 *     @author : never
 *     @since   : 2024/9/27
 * </pre>
 */
class GalleryPickerHelper {
    companion object {
        const val TAG = "GalleryPickerHelper"

        @JvmStatic
        fun newInstance(): GalleryPickerHelper = GalleryPickerHelper()

        @JvmStatic
        fun toPreViewVideo(activity: Activity, videoPath: String) {
            activity.startActivity(Intent(activity, VideoPreViewActivity::class.java).apply {
                putExtra("videoPath", videoPath)
            })
        }

        @JvmStatic
        fun toPreViewImage(activity: Activity, position: Int, images: ArrayList<String>) {
            activity.startActivity(Intent(activity, ImagePreViewActivity::class.java).apply {
                putExtra("select_position", position)
                putStringArrayListExtra("images", images)
            })
        }

        /**
         * 是否支持PhotoPicker
         * */
        @JvmStatic
        fun isPhotoPickerAvailable(context: Context): Boolean {
            return ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
        }

    }

    private var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var pickMultipleMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var cropResultLauncher: ActivityResultLauncher<Intent>? = null

    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null

    private var captureVideoLauncher: ActivityResultLauncher<Uri>? = null

    private var videoMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var imageOrVideoMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null


    private var mediaResultCallback: MediaResultCallback? = null

    private var photoFilePath: String = ""

    private var captureVideoFilePath: String = ""

    private var mMediaType = MediaType.IMAGE

    //是否压缩 默认压缩
    private var isCompress: Boolean = GalleryPickerOption.isCompress

    //是否裁剪  默认不裁剪 & 只支持当选裁剪
    private var isCrop: Boolean = GalleryPickerOption.isCrop

    //最大选择数量
    private var maxItems: Int = GalleryPickerOption.maxItems

    //最低压缩大小
    private var ignoreSize: Int = GalleryPickerOption.ignoreSize

    //压缩比例
    private var quality: Int = GalleryPickerOption.quality

    //支持获取视频文件大小,超过该大小则选择失败
    private var maxVideoSize: Int = GalleryPickerOption.maxVideoSize

    private var mLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            if (owner !is ComponentActivity) {
                return
            }
            initLaunch(owner)

        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            //移除自己,防止内存泄漏
            owner.lifecycle.removeObserver(this)
            LifecycleObserverHelper.removeObserver()

        }
    }


    /**
     * 是否压缩 默认压缩
     * @param compress true
     * */
    fun isCompress(compress: Boolean) = apply {
        this.isCompress = compress
    }

    /**
     * 是否裁剪 默认不裁剪 & 只支持单选
     * @param crop false
     * */
    fun isCrop(crop: Boolean) = apply {
        this.isCrop = crop
    }

    /**
     * 最大选择数量
     * @param maxItems  1 单选
     * */
    fun maxItems(maxItems: Int) = apply {
        this.maxItems = maxItems
    }

    /**
     * 当原始图像文件大小小于这个值时不进行压缩，单位为KB。
     * @param value 100kb
     */
    fun ignoreSize(value: Int) = apply { this.ignoreSize = value }

    /** 压缩图片比例，实际是传入[Bitmap.compress]方法的quality参数，默认值75% */
    fun quality(value: Int) = apply { this.quality = value }

    /**
     * 获取的视频文件最大大小
     * @param maxSize  15MB 单位MB
     * */
    fun maxVideoSize(maxSize: Int) = apply { this.maxVideoSize = maxSize }


    /**
     * 初始化启动器
     * */
    private fun initLaunch(activity: ComponentActivity) {
        //相机拍照
        if (mMediaType == MediaType.CAMERA) {
            takePictureLauncher = registerTakePictureVisualResult(activity, mediaResultCallback)
            takePictureLauncher?.let {
                activity.lifecycleScope.launch {
                    val file = Utils.createCameraFile(activity)
                    photoFilePath = file.path
                    val fileUri = FileProvider.getUriForFile(
                        activity, "${activity.applicationContext.packageName}.fileprovider", file
                    )
                    takePictureLauncher?.launch(fileUri)
                }
            }
            return
        }
        //相机拍摄
        if (mMediaType == MediaType.CAPTURE_VIDEO) {
            captureVideoLauncher = registerCaptureVideoVisualResult(activity, mediaResultCallback)
            captureVideoLauncher?.let {
                activity.lifecycleScope.launch {
                    val file = Utils.createCaptureVideoFile(activity)
                    captureVideoFilePath = file.path
                    val fileUri = FileProvider.getUriForFile(
                        activity, "${activity.applicationContext.packageName}.fileprovider", file
                    )
                    captureVideoLauncher?.launch(fileUri)
                }
            }
            return
        }


        //视频处理
        if (mMediaType == MediaType.VIDEO) {
            videoMediaLauncher = registerVideoVisualResult(activity, mediaResultCallback)
            videoMediaLauncher?.launch(PickVisualMediaRequest(createVisualMedia(mMediaType)))
            return
        }
        //图片or视频单选
        if (mMediaType == MediaType.IMAGE_OR_VIDEO) {
            imageOrVideoMediaLauncher =
                registerImageOrVideoVisualResult(activity, mediaResultCallback)
            imageOrVideoMediaLauncher?.launch(PickVisualMediaRequest(createVisualMedia(mMediaType)))
            return
        }

        //多选
        if (maxItems > 1) {
            pickMultipleMediaLauncher =
                registerPickMultipleVisualResult(activity, maxItems, mediaResultCallback)
            pickMultipleMediaLauncher?.launch(PickVisualMediaRequest(createVisualMedia(mMediaType)))
            return
        }
        //单选
        pickMediaLauncher = registerPickVisualResult(activity, mediaResultCallback)
        if (isCrop) {
            cropResultLauncher = registerCropVisualResult(activity, mediaResultCallback)
        }
        pickMediaLauncher?.launch(PickVisualMediaRequest(createVisualMedia(mMediaType)))


    }


    private fun createVisualMedia(mediaType: MediaType): ActivityResultContracts.PickVisualMedia.VisualMediaType {
        return when (mediaType) {
            MediaType.IMAGE -> {
                ActivityResultContracts.PickVisualMedia.ImageOnly
            }

            MediaType.VIDEO -> {
                ActivityResultContracts.PickVisualMedia.VideoOnly
            }

            MediaType.CAMERA -> {
                ActivityResultContracts.PickVisualMedia.ImageOnly
            }

            MediaType.IMAGE_OR_VIDEO -> {
                ActivityResultContracts.PickVisualMedia.ImageAndVideo
            }

            else -> ActivityResultContracts.PickVisualMedia.ImageOnly
        }
    }

    /**
     * 启动媒体选择器
     * @param activity
     * @param mediaType 媒体类型
     * @param callback 结果回调
     * */
    fun launchMediaPicker(activity: Activity, mediaType: MediaType, callback: MediaResultCallback) {
        LifecycleObserverHelper.removeObserver()
        LifecycleObserverHelper.bindObserver(mLifecycleObserver)
        mediaResultCallback = callback
        mMediaType = mediaType
        //创建一个空页面进行相册处理
        activity.startActivity(Intent(activity, EmptyHandleActivity::class.java))
    }


    /**
     * 使用 ActivityResult 回调方式需要再 onResume() 之前注册
     * @param activity 继承自ComponentActivity 的activity
     * @param block 返回获取到的图片uri
     * */
    private fun registerPickVisualResult(
        activity: ComponentActivity, block: MediaResultCallback?
    ): ActivityResultLauncher<PickVisualMediaRequest> {

        return activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                // 取消选择，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("PickVisual  fail-> User canceled selection")
            }
            if (uri != null) {
                Utils.log("PickVisual  success->   $uri")
                val uris = mutableListOf<Uri>()
                uris.add(uri)
                handleImageUri(activity, uris, block)
            }
        }

    }

    /**
     * 使用 ActivityResult 回调方式需要再 onResume() 之前注册
     * @param activity 继承自ComponentActivity 的activity
     * @param maxItems 最大选择图片数量
     * @param block 返回获取到的图片uris
     * */
    private fun registerPickMultipleVisualResult(
        activity: ComponentActivity, maxItems: Int = 1, block: MediaResultCallback?
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return activity.registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(
                maxItems
            )
        ) { uris ->
            if (uris.isEmpty()) {
                // 取消选择，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("PickVisual  fail -> User canceled multiple selection")
            }
            if (uris.isNotEmpty()) {
                Utils.log("PickVisual  success  uris: $uris")
                activity.lifecycleScope.launch {
                    // 在 IO 线程执行
                    val files = copyFilesToCache(activity, uris)
                    val tempFiles = mutableListOf<File>()
                    files.forEach { file ->
                        if (file != null && file.exists()) {
                            tempFiles.add(file)
                        }
                    }
                    if (tempFiles.isNotEmpty()) {
                        if (isCompress) {
                            //批量压缩
                            Utils.log("Start compression }")
                            val result = Luban(activity).load(tempFiles).ignoreSize(ignoreSize)
                                .quality(quality).keepAlpha(false).keepSize(false).launch()
                            if (result.isNotEmpty()) {
                                val filePaths = mutableListOf<String>()
                                val mediaFiles = mutableListOf<MediaData>()
                                result.forEach { compressResult ->
                                    if (compressResult is CompressResult.Success) {
                                        filePaths.add(compressResult.file?.path ?: "")
                                        mediaFiles.add(
                                            MediaData(
                                                MimeType.IMAGE,
                                                compressResult.file?.path ?: ""
                                            )
                                        )
                                    }
                                }
                                Utils.log("Compression successful  -> path:  $filePaths")
                                block?.onResult(filePaths)
                                block?.onMediaResult(mediaFiles)
                                activity.finish()
                                Utils.log("MediaResultCallback  result  path: $filePaths")
                            }
                        } else {
                            val filePaths = mutableListOf<String>()
                            val mediaFiles = mutableListOf<MediaData>()
                            tempFiles.forEach { file ->
                                filePaths.add(file.path)
                                mediaFiles.add(
                                    MediaData(
                                        MimeType.IMAGE,
                                        file.path
                                    )
                                )
                            }
                            block?.onResult(filePaths)
                            block?.onMediaResult(mediaFiles)
                            activity.finish()
                            Utils.log("MediaResultCallback  result  path: $filePaths")

                        }
                    }
                }
            }
        }
    }

    /**
     * 相机拍照
     * 使用 ActivityResult 回调方式需要再 onResume() 之前注册
     * @param activity 继承自ComponentActivity 的activity
     * @param block 返回获取到的图片uri
     * */
    private fun registerTakePictureVisualResult(
        activity: ComponentActivity, block: MediaResultCallback?
    ): ActivityResultLauncher<Uri> {
        return activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (!success) {
                // 取消拍照，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("TakePicture canceled")
            }
            //拍照成功，处理返回的照片
            if (success) {
                Utils.log("TakePicture success")
                if (photoFilePath.isNotEmpty()) {
                    activity.lifecycleScope.launch {
                        val file = File(photoFilePath)
                        if (file.exists()) {
                            Utils.log("TakePicture success  file: $photoFilePath")
                            if (isCompress) {
                                val result = Luban(activity).load(file).ignoreSize(ignoreSize)
                                    .quality(quality).keepAlpha(false).keepSize(false).single()
                                // 只处理一个
                                handleResult(activity, result, block)
                            } else {
                                handleCrop(activity, file.path, block)
                            }
                        }
                    }

                } else {
                    Utils.log("TakePicture success  But the temporary storage file path is empty")
                }
            }
        }
    }

    private fun registerCaptureVideoVisualResult(
        activity: ComponentActivity,
        block: MediaResultCallback?
    ): ActivityResultLauncher<Uri> {
        return activity.registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
            if (!success) {
                // 取消拍摄，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("CaptureVideo canceled")
            }
            //拍摄成功，处理返回的视频
            if (success) {
                Utils.log("CaptureVideo success")
                if (captureVideoFilePath.isNotEmpty()) {
                    activity.lifecycleScope.launch {
                        val file = File(captureVideoFilePath)
                        if (file.exists()) {
                            Utils.log("CaptureVideo success  file: $captureVideoFilePath")
                            val mediaFiles = mutableListOf<MediaData>()
                            mediaFiles.add(MediaData(MimeType.VIDEO, file.path))
                            block?.onMediaResult(mediaFiles)
                            activity.finish()
                        }
                    }

                } else {
                    Utils.log("CaptureVideo success  But the temporary storage file path is empty")
                }
            }
        }
    }

    /**
     * 视频选择
     * 使用 ActivityResult 回调方式需要再 onResume() 之前注册
     * @param activity 继承自ComponentActivity 的activity
     * @param block 返回获取到的图片uri
     * */
    private fun registerVideoVisualResult(
        activity: ComponentActivity, block: MediaResultCallback?
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                // 取消视频选择，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("PickVisual video fail -> User canceled video selection")
            }
            if (uri != null) {
                Utils.log("PickVisual  success->   $uri")
                handleVideoUri(activity, uri, block)
            }
        }
    }

    /**
     * 视频or图片选择
     * 使用 ActivityResult 回调方式需要再 onResume() 之前注册
     * @param activity 继承自ComponentActivity 的activity
     * @param block 返回获取到的图片uri
     * */
    private fun registerImageOrVideoVisualResult(
        activity: ComponentActivity, block: MediaResultCallback?
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                // 取消视频选择，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("PickVisual  fail -> User canceled  selection")
            }
            if (uri != null) {
                val contentResolver: ContentResolver = activity.contentResolver
                val mimeType = contentResolver.getType(uri!!)
                if (mimeType != null) {
                    when {
                        mimeType.startsWith("image/") -> {
                            Log.d("PickMedia", "选中的是图片: $mimeType")
                            handleImageUri(activity, mutableListOf(uri), block)
                        }

                        mimeType.startsWith("video/") -> {
                            Log.d("PickMedia", "选中的是视频: $mimeType")
                            handleVideoUri(activity, uri, block)
                        }

                        else -> {
                            block?.onCancel()
                            activity.finish()
                            Log.d("PickMedia", "未知类型: $mimeType")
                        }
                    }
                } else {
                    block?.onCancel()
                    activity.finish()
                    Log.d("PickMedia", "无法检测到类型，可能文件已被移动或权限不足")
                }
            }

        }
    }

    private fun registerCropVisualResult(
        activity: ComponentActivity, block: MediaResultCallback?
    ): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val resultPath = data?.getStringExtra("result_path") ?: ""
                val list = mutableListOf<String>()
                val mediaFiles = mutableListOf<MediaData>()
                mediaFiles.add(MediaData(MimeType.IMAGE, resultPath))
                list.add(resultPath)
                block?.onResult(list)
                block?.onMediaResult(mediaFiles)
                activity.finish()
                Utils.log("MediaResultCallback  result  path: $resultPath")
            } else {
                // 取消裁剪，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("Cancel cropping")
            }
        }
    }

    /**
     * 处理选择的视频文件
     * */
    private fun handleVideoUri(activity: ComponentActivity, uri: Uri, block: MediaResultCallback?) {
        activity.lifecycleScope.launch() {
            // 获取文件大小并在 I/O 线程中执行
            val fileSizeInBytes = getFileSizeFromUri(activity, uri)
            Utils.log("PickVisual video file size: $fileSizeInBytes")
            if (fileSizeInBytes > maxVideoSize * 1024 * 1024) {
                Utils.showToast(
                    activity,
                    activity.getString(R.string.photo_video_size_tip) + "${maxVideoSize}M"
                )
                // 视频太大，当做取消处理，调用onCancel回调
                block?.onCancel()
                activity.finish()
                Utils.log("PickVisual video fail: File too large, please select a smaller one")
                return@launch
            }
            val uris = mutableListOf<Uri>()
            uris.add(uri)
            // 在 IO 线程执行
            val files = copyFilesToCache(activity, uris)
            files.forEach { file ->
                if (file != null && file.exists()) {
                    val list = mutableListOf<String>()
                    val mediaFiles = mutableListOf<MediaData>()
                    mediaFiles.add(MediaData(MimeType.VIDEO, file.path))
                    list.add(file.path)
                    block?.onResult(list)
                    block?.onMediaResult(mediaFiles)
                    activity.finish()
                    Utils.log("MediaResultCallback  result  path: $list")
                }
            }
        }
    }

    /**
     * 处理选择的图片文件
     * */
    private fun handleImageUri(
        activity: ComponentActivity,
        uris: List<Uri>,
        block: MediaResultCallback?
    ) {
        activity.lifecycleScope.launch() {
            // 在 IO 线程执行
            val files = copyFilesToCache(activity, uris)
            files.forEach { file ->
                if (file != null && file.exists()) {
                    if (isCompress) {
                        Utils.log("Start compression }")
                        val result = Luban(activity).load(file).ignoreSize(ignoreSize)
                            .quality(quality).keepAlpha(false).keepSize(false).single()
                        // 只处理一个
                        handleResult(activity, result, block)
                    } else {
                        handleCrop(activity, file.path, block)
                    }
                }
            }
        }
    }

    private suspend fun copyFilesToCache(context: Context, uris: List<Uri>): List<File?> {
        return uris.map { uri ->
            flow {
                val contentResolver: ContentResolver = context.contentResolver
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                try {
                    val cacheDir =
                        File(context.cacheDir.absolutePath, "/temp_photo").also { it.mkdirs() }
                    val random = Random.nextInt(1000)
                    val suffix = if (mMediaType == MediaType.VIDEO) "mp4" else "jpg"
                    val cacheFile = File(cacheDir, "${System.currentTimeMillis()}$random.$suffix")
                    FileOutputStream(cacheFile).use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                    emit(cacheFile) // 复制成功，发射新文件
                    Utils.log("File copy success   file: ${cacheFile.path}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    emit(null) // 复制失败，发射 null
                    Utils.log("File copy creation failed   uri: $uri")
                } finally {
                    inputStream?.close() // 确保关闭 InputStream
                }
            }.flowOn(Dispatchers.IO)
        }.map { flow ->
            flow.toList() // 收集每个 `Flow` 结果到列表
        }.flatten() // 扁平化为一个 `List<File?>`
    }

    /**
     * 获取文件大小
     * @param context
     * @param uri 文件地址
     * */
    private suspend fun getFileSizeFromUri(context: Context, uri: Uri): Long =
        withContext(Dispatchers.IO) {
            var fileSize: Long = 0
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                fileSize = cursor.getLong(sizeIndex)
            }
            fileSize
        }


    private fun handleResult(
        activity: ComponentActivity, result: CompressResult, block: MediaResultCallback?
    ) {
        when (result) {
            is CompressResult.Start -> {

            }

            is CompressResult.Success -> {
                Utils.log("Compression successful  -> path:  ${result.file?.path}")
                handleCrop(activity, result.file?.path ?: "", block)
            }

            is CompressResult.Error -> {
                Utils.log("Compression fail  -> path:  ${result.e.message}")
                // 压缩失败，调用onCancel回调
                block?.onCancel()
                activity.finish()
            }

            CompressResult.Completed -> {

            }
        }
    }

    private fun handleCrop(
        activity: ComponentActivity, filePath: String, block: MediaResultCallback?
    ) {
        if (filePath.isEmpty() || !isCrop) {
            val list = mutableListOf<String>()
            list.add(filePath)
            val mediaFiles = mutableListOf<MediaData>()
            mediaFiles.add(MediaData(MimeType.IMAGE, filePath))
            block?.onResult(list)
            block?.onMediaResult(mediaFiles)
            activity.finish()
            Utils.log("MediaResultCallback  result  path: $filePath")
            return
        }
        //跳转裁剪
        val intent = Intent(activity, ClipPhotoActivity::class.java)
        intent.putExtra("path", filePath)
        cropResultLauncher?.launch(intent)
    }

}
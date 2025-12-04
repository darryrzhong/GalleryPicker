package com.photo.picker.callback

import com.photo.picker.MediaData
import com.photo.picker.MediaType
import com.photo.picker.MimeType

/**
 * <pre>
 *     类描述  : 媒体选择回调
 *
 *
 *     @author : never
 *     @since   : 2024/10/11
 * </pre>
 */
interface MediaResultCallback {

    /**
     * 选择结果回调
     * @param filePaths 文件地址集合
     * */
    @Deprecated(
        message = "请使用 onMediaResult(List<MediaItem>) 代替。",
        replaceWith = ReplaceWith("onMediaResult(filePaths)"),
        level = DeprecationLevel.WARNING //
    )
    fun onResult(filePaths: List<String>) {
    }

    /**
     * 选择结果回调
     * @param mediaFiles 文件数据集合
     * */
    fun onMediaResult(mediaFiles: List<MediaData>)

    /**
     * 取消选择回调
     * 当用户未选择媒体而取消操作时调用
     * 此方法为可选实现，默认为空实现
     * */
    fun onCancel() {}
}
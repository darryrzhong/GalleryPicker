package com.photo.picker.luban

import java.io.File

/**
 * 压缩结果
 *
 * @author never
 * @date 2024/09/29
 */
sealed class CompressResult {

    /** 开始压缩 */
    class Start(val path: String?, val current: Int, val total: Int) : CompressResult()

    /** 压缩成功，逐个回调 */
    class Success(val file: File?, val current: Int, val total: Int) : CompressResult()

    /** 压缩失败 */
    class Error(val path: String?, val e: Throwable) : CompressResult()

    /** 所有任务完成 */
    object Completed : CompressResult()
}
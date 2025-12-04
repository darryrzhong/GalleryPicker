package com.photo.picker.luban

import androidx.annotation.RestrictTo
import com.photo.picker.luban.BaseInputStreamProvider
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 文件流
 *
 * @author never
 * @date 2024/09/29
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class FileInputStreamProvider(private val file: File) : BaseInputStreamProvider() {

    override val path: String get() = file.absolutePath

    override fun openInternal(): InputStream = FileInputStream(file)
}

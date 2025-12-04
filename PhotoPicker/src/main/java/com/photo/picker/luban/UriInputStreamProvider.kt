package com.photo.picker.luban

import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import com.photo.picker.luban.BaseInputStreamProvider
import java.io.InputStream

/**
 * Uri流
 *
 * @author never
 * @date 2024/09/29
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class UriInputStreamProvider(private val context: Context, private val uri: Uri) :
    BaseInputStreamProvider() {

    override val path: String? get() = uri.path

    override fun openInternal(): InputStream? = context.contentResolver.openInputStream(uri)
}

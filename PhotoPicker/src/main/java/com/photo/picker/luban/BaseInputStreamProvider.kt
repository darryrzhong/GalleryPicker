package com.photo.picker.luban

import androidx.annotation.RestrictTo
import com.photo.picker.luban.InputStreamProvider
import java.io.IOException
import java.io.InputStream

/**
 * Automatically close the previous InputStream when opening a new InputStream,
 * and finally need to manually call [.close] to release the resource.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal abstract class BaseInputStreamProvider : InputStreamProvider {
    private var inputStream: InputStream? = null

    @Throws(IOException::class)
    override fun open(): InputStream? {
        close()
        inputStream = openInternal()
        return inputStream
    }

    @Throws(IOException::class)
    abstract fun openInternal(): InputStream?

    override fun close() {
        if (inputStream != null) {
            try {
                inputStream!!.close()
            } catch (ignore: IOException) {
            } finally {
                inputStream = null
            }
        }
    }
}
package com.photo.picker

import java.io.Serializable

/**
 * <pre>
 *     类描述  :
 *
 *     @author : never
 *     @since  : 2025/10/22
 * </pre>
 */
data class MediaData(var mimeType: MimeType, var filePath: String) : Serializable
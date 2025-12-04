package com.photo.picker.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.photo.picker.R
import com.photo.picker.photoview.PhotoView

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/14
 * </pre>
 */
class ImagePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var photoView: PhotoView? = null

    init {
        photoView = itemView.findViewById(R.id.preview_image)
    }
}
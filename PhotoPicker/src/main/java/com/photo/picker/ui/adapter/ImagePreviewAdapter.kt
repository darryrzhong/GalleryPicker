package com.photo.picker.ui.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.photo.picker.R

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/14
 * </pre>
 */
class ImagePreviewAdapter(val data: MutableList<String>) :
    RecyclerView.Adapter<ImagePreviewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.item_image_preview_layout, parent, false)
        return ImagePreviewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ImagePreviewHolder, position: Int) {
        val image = data[position]
        var preview = holder.photoView
        preview?.let {
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(image)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        it.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
        }

    }
}


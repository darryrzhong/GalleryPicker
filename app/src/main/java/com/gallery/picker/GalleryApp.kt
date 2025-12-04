package com.gallery.picker

import android.app.Application
import com.photo.picker.GalleryPickerOption

/**
 * <pre>
 *     类描述  :
 *
 *     @author : never
 *     @since  : 2025/11/24
 * </pre>
 */
class GalleryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //相册选择库
        GalleryPickerOption.setColorPrimary(com.google.android.material.R.color.design_default_color_primary) //设置app主题色
            .setTextColorPrimary(R.color.white) //设置app主题色搭配字体色
            .maxItems(1) //设置最大选择数量  1 单选  >1多选
            .isCompress(true) //默认图片压缩
            .ignoreSize(100)  // 小于100kb文件不进行压缩
            .quality(75)  //压缩比例 默认75%
            .isCrop(false) //是否进行图片裁剪  默认不裁剪
            .maxVideoSize(100) //最大选择视频文件大小  默认15Mb
            .debug(true)  //日志调试
    }
}
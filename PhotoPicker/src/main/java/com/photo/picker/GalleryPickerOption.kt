package com.photo.picker

import android.graphics.Bitmap

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/11
 * </pre>
 */
object GalleryPickerOption {
    //app主题色
    internal var colorPrimary: Int = R.color.photo_white

    //app主题色下的字体颜色
    internal var textColorPrimary: Int = R.color.photo_black

    //是否压缩 默认压缩
    internal var isCompress: Boolean = true

    //是否裁剪  默认不裁剪
    internal var isCrop: Boolean = false

    //最大选择数量
    internal var maxItems: Int = 1

    //最低压缩大小
    internal var ignoreSize: Int = 100

    //压缩比例
    internal var quality: Int = 75

    //支持获取视频文件大小,超过该大小则选择失败
    internal var maxVideoSize: Int = 15

    //debug 输出日志
    internal var debug: Boolean = false

    /**
     * 自定义app主题色
     * @param colorPrimary
     * */
    fun setColorPrimary(colorPrimary: Int): GalleryPickerOption = apply {
        this.colorPrimary = colorPrimary

    }

    /**
     * 自定义app主题色适配的字体色
     * @param colorPrimary
     * */
    fun setTextColorPrimary(colorPrimary: Int): GalleryPickerOption = apply {
        this.textColorPrimary = colorPrimary

    }


    /**
     * 是否压缩 默认压缩
     * @param compress true
     * */
    fun isCompress(compress: Boolean) = apply {
        this.isCompress = compress
    }

    /**
     * 是否裁剪 默认不裁剪 & 只支持单选,多选此选项不生效
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
     * 视频文件的大小
     * @param maxSize  15MB 单位MB
     * */
    fun maxVideoSize(maxSize: Int) = apply {
        this.maxVideoSize = maxSize
    }


    /**
     * 当原始图像文件大小小于这个值时不进行压缩，单位为KB。
     * @param value 100kb
     */
    fun ignoreSize(value: Int) = apply { this.ignoreSize = value }

    /** 压缩图片比例，实际是传入[Bitmap.compress]方法的quality参数，默认值75% */
    fun quality(value: Int) = apply { this.quality = value }

    /**
     * 调试模式
     * @param debug false
     * */
    fun debug(debug: Boolean) = apply {
        this.debug = debug
    }
}
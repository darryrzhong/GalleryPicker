package com.photo.picker.crop.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.RelativeLayout

/**
 * 剪切视图
 */
class ClipImageLayout(private val mContext: Context, attrs: AttributeSet?) :
    RelativeLayout(mContext, attrs) {
    private val mZoomImageView: ClipZoomImageView
    private val mClipImageView: ClipImageBorderView

    //    private int type = 0;//0 16:9  1 其他
    private var bitmap: Bitmap? = null

    /**
     * 这里测试，直接写死了大小，真正使用过程中，可以提取为自定义属性
     */
    private var mHorizontalPadding = 1
    private var mVerticalPadding = (height - (width - 2 * mHorizontalPadding)) / 2

    init {
        mZoomImageView = ClipZoomImageView(context)
        mClipImageView = ClipImageBorderView(context)
        val lp = LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        /**
         * 这里，直接写死了图片，
         */
        // mZoomImageView.setImageDrawable(getResources().getDrawable(
        // R.drawable.a));
        this.addView(mZoomImageView, lp)
        this.addView(mClipImageView, lp)

        // 计算padding的px
        mHorizontalPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding.toFloat(), resources
                .displayMetrics
        ).toInt()
        mZoomImageView.setHorizontalPadding(mHorizontalPadding)
        mClipImageView.setHorizontalPadding(mHorizontalPadding)
        mZoomImageView.setVerticalPadding(mVerticalPadding)
        mClipImageView.setVerticalPadding(mVerticalPadding)
    }

    fun setImageDrawable(drawable: Drawable?) {
        mZoomImageView.setImageDrawable(drawable)
    }

    //    public void setImageDrawable(String path) {
    //        //显示本地图片  设置本地图片
    ////        String showPath = ImageDownloader.Scheme.FILE.wrap(path);
    ////        bitmap = BitmapUtils.INSTANCE.decodeFile(path);
    //        if (bitmap != null) {
    //            mZoomImageView.setImageBitmap(bitmap);
    //        }
    ////        ImageLoader.getInstance().displayImage(showPath, mZoomImageView);
    ////		mZoomImageView.setImageDrawable(drawable);
    //    }
    fun setImageBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            this.bitmap = bitmap
            mZoomImageView.setImageBitmap(bitmap)
        }
    }

    /**
     * 对外公布设置边距的方法,单位为dp
     *
     * @param mHorizontalPadding
     */
    fun setHorizontalPadding(mHorizontalPadding: Int) {
        this.mHorizontalPadding = mHorizontalPadding
    }

    /**
     * 设置宽高比例
     *
     * @param widthProportion
     * @param heightProportion
     */
    fun setProportion(widthProportion: Int, heightProportion: Int) {
        mClipImageView.setProportion(widthProportion, heightProportion)
        mZoomImageView.setProportion(widthProportion, heightProportion)
    }

    fun setVerticalPadding(mVerticalPadding: Int) {
        this.mVerticalPadding = mVerticalPadding
    }

    /**
     * 裁切图片
     *
     * @return
     */
    fun clip(): Bitmap {
        return mZoomImageView.clip()
    }

    fun setRotaingImageView(angle: Int) {
        mZoomImageView.setImageBitmap(adjustPhotoRotation(bitmap, angle))
    }

    fun adjustPhotoRotation(bm: Bitmap?, orientationDegree: Int): Bitmap? {
        val m = Matrix()
        try {
            m.setRotate(orientationDegree.toFloat(), bm!!.width.toFloat(), bm.height.toFloat())
            return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
        } catch (ex: Exception) {
        }
        return null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
    }

    companion object {
        /**
         * 旋转图片
         *
         * @param angle  旋转角度
         * @param bitmap 要处理的Bitmap
         * @return 处理后的Bitmap
         */
        fun rotaingImageView(angle: Int, bitmap: Bitmap?): Bitmap? {
            var bitmap = bitmap
            // 旋转图片 动作
            val matrix = Matrix()
            matrix.postRotate(angle.toFloat())
            // 创建新的图片
            val resizedBitmap = Bitmap.createBitmap(
                bitmap!!, 0, 0,
                bitmap.width, bitmap.height, matrix, true
            )
            if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                bitmap = null
            }
            return resizedBitmap
        }
    }
}

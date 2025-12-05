package com.photo.picker.crop.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import com.photo.picker.utils.Utils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class ClipZoomImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ImageView(context, attrs), OnScaleGestureListener, OnTouchListener, OnGlobalLayoutListener {
    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private var initScale = 1.0f
    private var once = true

    /**
     * 用于存放矩阵的9个值
     */
    private val matrixValues = FloatArray(9)

    /**
     * 缩放的手势检测
     */
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private val mScaleMatrix = Matrix()

    /**
     * 用于双击检测
     */
    private val mGestureDetector: GestureDetector
    private var isAutoScale = false
    private val mTouchSlop: Int = 0
    private var mLastX = 0f
    private var mLastY = 0f
    private var isCanDrag = false
    private var lastPointerCount = 0

    /**
     * 图片的比例 宽度
     */
    private var widthProportion = 10

    /**
     * 图片的比例高度
     */
    private var heightProportion = 7

    /**
     * 水平方向与View的边距
     */
    private var mHorizontalPadding = 0

    /**
     * 垂直方向与View的边距
     */
    private var mVerticalPadding = 0

    init {
        scaleType = ScaleType.MATRIX
        mGestureDetector = GestureDetector(context,
            object : SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (isAutoScale == true) return true
                    val x = e.x
                    val y = e.y
                    if (scale < SCALE_MID) {
                        this@ClipZoomImageView.postDelayed(
                            AutoScaleRunnable(SCALE_MID, x, y), 16
                        )
                        isAutoScale = true
                    } else {
                        this@ClipZoomImageView.postDelayed(
                            AutoScaleRunnable(initScale, x, y), 16
                        )
                        isAutoScale = true
                    }
                    return true
                }
            })
        mScaleGestureDetector = ScaleGestureDetector(context, this)
        this.setOnTouchListener(this)
    }

    /**
     * 设置宽高比例
     *
     * @param widthProportion
     * @param heightProportion
     */
    fun setProportion(widthProportion: Int, heightProportion: Int) {
        this.widthProportion = widthProportion
        this.heightProportion = heightProportion
    }

    /**
     * 自动缩放的任务
     *
     * @author zhy
     */
    private inner class AutoScaleRunnable(
        private val mTargetScale: Float,
        /**
         * 缩放的中心
         */
        private val x: Float,
        private val y: Float
    ) : Runnable {
        private var tmpScale: Float

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         */
        init {
            if (scale < mTargetScale) {
                tmpScale = BIGGER
            } else {
                tmpScale = SMALLER
            }
        }

        override fun run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y)
            checkBorder()
            imageMatrix = mScaleMatrix
            val currentScale = scale
            // 如果值在合法范围内，继续缩放
            if (tmpScale > 1f && currentScale < mTargetScale
                || tmpScale < 1f && mTargetScale < currentScale
            ) {
                this@ClipZoomImageView.postDelayed(this, 16)
            } else {
                val deltaScale = mTargetScale / currentScale
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y)
                checkBorder()
                imageMatrix = mScaleMatrix
                isAutoScale = false
            }
        }

    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scale = scale
        var scaleFactor = detector.scaleFactor
        if (drawable == null) return true

        /**
         * 缩放的范围控制
         */
        if (scale < SCALE_MAX && scaleFactor > 1.0f
            || scale > initScale && scaleFactor < 1.0f
        ) {
            /**
             * 最大值最小值判断
             */
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale
            }
            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale
            }
            /**
             * 设置缩放比例
             */
            mScaleMatrix.postScale(
                scaleFactor, scaleFactor,
                detector.focusX, detector.focusY
            )
            checkBorder()
            imageMatrix = mScaleMatrix
        }
        return true
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private val matrixRectF: RectF
        private get() {
            val matrix = mScaleMatrix
            val rect = RectF()
            val d = drawable
            if (null != d) {
                rect.set(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
                matrix.mapRect(rect)
            }
            return rect
        }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {}
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mGestureDetector.onTouchEvent(event)) return true
        mScaleGestureDetector!!.onTouchEvent(event)
        var x = 0f
        var y = 0f
        // 拿到触摸点的个数
        val pointerCount = event.pointerCount
        // 得到多个触摸点的x与y均值
        for (i in 0 until pointerCount) {
            x += event.getX(i)
            y += event.getY(i)
        }
        x = x / pointerCount
        y = y / pointerCount

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false
            mLastX = x
            mLastY = y
        }
        lastPointerCount = pointerCount
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx = x - mLastX
                var dy = y - mLastY
                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy)
                }
                if (isCanDrag) {
                    if (drawable != null) {
                        val rectF = matrixRectF
                        // 如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() <= width - mHorizontalPadding * 2) {
                            dx = 0f
                        }
                        // 如果高度小雨屏幕高度，则禁止上下移动
                        if (rectF.height() <= height - mVerticalPadding * 2) {
                            dy = 0f
                        }
                        mScaleMatrix.postTranslate(dx, dy)
                        checkBorder()
                        imageMatrix = mScaleMatrix
                    }
                }
                mLastX = x
                mLastY = y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> lastPointerCount = 0
        }
        return true
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    val scale: Float
        get() {
            mScaleMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeGlobalOnLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (once) {
            val d = drawable ?: return
            // 垂直方向的边距  注意 这里是实际裁剪的边距( 整个图片的高度 减去这个裁剪区域的高度 再除以2 就是实际裁剪区域 的垂直方向的边距)
            mVerticalPadding = max(
                0.0,
                ((height - Utils.getHeight(widthProportion, heightProportion, width)) / 2).toDouble()
            ).toInt()
            // mVerticalPadding = (getHeight() - (getWidth() - 2 *
            // mHorizontalPadding)) / 2 ;
            // Utils.showLog("距离垂直方向的边距"+mVerticalPadding + "  高度 = "
            // +getHeight() + " 宽度 = " + getWidth());
            val width = width
            val height = height
            // 拿到图片的宽和高
            val dw = d.intrinsicWidth
            val dh = d.intrinsicHeight
            var scale = 1.0f
            if (dw >= getWidth() - mHorizontalPadding * 2
                && dh >= getHeight() - mVerticalPadding * 2
            ) {
                val scaleW = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw
                val scaleH = (getHeight() * 1.0f - mVerticalPadding * 2) / dh
                scale = max(scaleW.toDouble(), scaleH.toDouble()).toFloat()
            }
            if (dw < getWidth() - mHorizontalPadding * 2
                && dh > getHeight() - mVerticalPadding * 2
            ) {
                scale = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw
            }
            if (dh < getHeight() - mVerticalPadding * 2
                && dw > getWidth() - mHorizontalPadding * 2
            ) {
                scale = (getHeight() * 1.0f - mVerticalPadding * 2) / dh
            }
            if (dw < getWidth() - mHorizontalPadding * 2
                && dh < getHeight() - mVerticalPadding * 2
            ) {
                val scaleW = (getWidth() * 1.0f - mHorizontalPadding * 2) / dw
                val scaleH = (getHeight() * 1.0f - mVerticalPadding * 2) / dh
                scale = max(scaleW.toDouble(), scaleH.toDouble()).toFloat()
            }
            initScale = scale
            SCALE_MID = initScale * 2
            SCALE_MAX = initScale * 4
            mScaleMatrix.postTranslate(((width - dw) / 2).toFloat(), ((height - dh) / 2).toFloat())
            mScaleMatrix.postScale(
                scale, scale, (getWidth() / 2).toFloat(),
                (getHeight() / 2).toFloat()
            )
            // 图片移动至屏幕中心
            imageMatrix = mScaleMatrix
            once = false
        }
    }

    /**
     * 剪切图片，返回剪切后的bitmap对象
     *
     * @return
     */
    fun clip(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            width, height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        draw(canvas) // Bitmap source, int x, int y, int width, int
        // height)//实际在这里剪切

        // 边界检查，确保裁剪参数有效
        val x = max(0.0, mHorizontalPadding.toDouble()).toInt()
        val y = max(0.0, mVerticalPadding.toDouble()).toInt()
        var width = min((getWidth() - 2 * mHorizontalPadding).toDouble(), (getWidth() - x).toDouble())
            .toInt()
        var height = min(
            Utils.getHeight(widthProportion, heightProportion, getWidth()).toDouble(),
            (getHeight() - y).toDouble()
        ).toInt()

        // 确保宽高为正数
        width = max(1.0, width.toDouble()).toInt()
        height = max(1.0, height.toDouble()).toInt()
        val bitmap1 = Bitmap.createBitmap(bitmap, x, y, width, height)
        // 如果原图 图片还没有回收，强制回收
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
        return bitmap1
    }

    /**
     * 边界检测
     */
    private fun checkBorder() {
        val rect = matrixRectF
        var deltaX = 0f
        var deltaY = 0f
        val width = width
        val height = height
        /*LogUtils.d("rect.width() =  " + rect.width()
                + " , width - 2 * mHorizontalPadding ="
				+ (width - 2 * mHorizontalPadding));*/

        // 如果宽或高大于屏幕，则控制范围 ; 这里的0.001是因为精度丢失会产生问题，但是误差一般很小，所以我们直接加了一个0.01
        if (rect.width() + 0.01 >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right
            }
        }
        if (rect.height() + 0.01 >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom
            }
        }
        mScaleMatrix.postTranslate(deltaX, deltaY)
    }

    /**
     * 是否是拖动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private fun isCanDrag(dx: Float, dy: Float): Boolean {
        return sqrt((dx * dx + dy * dy).toDouble()) >= mTouchSlop
    }

    fun setHorizontalPadding(mHorizontalPadding: Int) {
        this.mHorizontalPadding = mHorizontalPadding
    }

    fun setVerticalPadding(mVerticalPadding: Int) {
        this.mVerticalPadding = mVerticalPadding
    }

    companion object {
        const val BIGGER = 1.07f
        const val SMALLER = 0.93f

        private val TAG = ClipZoomImageView::class.java.simpleName
        var SCALE_MAX = 4.0f
        private var SCALE_MID = 2.0f
    }
}

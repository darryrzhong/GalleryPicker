package com.photo.picker.crop.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * @author zhy http://blog.csdn.net/lmj623565791/article/details/39761281
 * 绘制阴影截图视图
 */
class ClipImageBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    /**
     * 水平方向与View的边距
     */
    private var mHorizontalPadding = 0

    /**
     * 垂直方向与View的边距
     */
    private var mVerticalPadding = 0

    /**
     * 绘制的矩形的宽度
     */
    private var mWidth = 0

    /**
     * 边框的颜色，默认为白色
     */
    private val mBorderColor = Color.parseColor("#FFFFFF")

    /**
     * 边框的宽度 单位dp
     */
    private var mBorderWidth = 1

    /**
     * 图片的比例 宽度
     */
    private var widthProportion = 1

    /**
     * 图片的比例高度
     */
    private var heightProportion = 1
    private val mPaint: Paint

    init {
        mBorderWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, mBorderWidth.toFloat(), resources
                .displayMetrics
        ).toInt()
        mPaint = Paint()
        mPaint.isAntiAlias = true
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 计算矩形区域的宽度
        mWidth = width - 2 * mHorizontalPadding
        var height = 0
        // 计算距离屏幕垂直边界 的边距
        height = if (widthProportion == 1 && heightProportion == 1) {
            mWidth
        } else {
            width / widthProportion * heightProportion
        }


        mVerticalPadding = (getHeight() - height) / 2
        // mVerticalPadding = (getHeight() - mWidth) / 2;
        // mVerticalPadding = (getHeight() - (getWidth() - 2 *
        // mHorizontalPadding)) / 2;
        mPaint.color = Color.parseColor("#aa000000")
        mPaint.style = Style.FILL
        // 绘制左边1
        canvas.drawRect(0f, 0f, mHorizontalPadding.toFloat(), getHeight().toFloat(), mPaint)
        // 绘制右边2
        canvas.drawRect(
            (width - mHorizontalPadding).toFloat(), 0f, width.toFloat(),
            getHeight().toFloat(), mPaint
        )
        // 绘制上边3
        canvas.drawRect(
            mHorizontalPadding.toFloat(), 0f, (width - mHorizontalPadding).toFloat(),
            mVerticalPadding.toFloat(), mPaint
        )
        // 绘制下边4
        canvas.drawRect(
            mHorizontalPadding.toFloat(), (getHeight() - mVerticalPadding).toFloat(),
            (width - mHorizontalPadding).toFloat(), getHeight().toFloat(), mPaint
        )
        // 绘制外边框
        mPaint.color = mBorderColor
        mPaint.strokeWidth = mBorderWidth.toFloat()
        mPaint.style = Style.STROKE
        canvas.drawRect(
            mHorizontalPadding.toFloat(), mVerticalPadding.toFloat(), (width
                    - mHorizontalPadding).toFloat(), (getHeight() - mVerticalPadding).toFloat(), mPaint
        )
    }

    fun setHorizontalPadding(mHorizontalPadding: Int) {
        this.mHorizontalPadding = mHorizontalPadding
    }

    fun setVerticalPadding(mVerticalPadding: Int) {
        this.mVerticalPadding = mVerticalPadding
    }
}

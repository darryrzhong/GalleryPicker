package com.photo.picker.crop.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.photo.picker.utils.BitmapUtils;

/**
 * 剪切视图
 */
public class ClipImageLayout extends RelativeLayout {

    private ClipZoomImageView mZoomImageView;
    private ClipImageBorderView mClipImageView;
//    private int type = 0;//0 16:9  1 其他

    private Context mContext;

    private Bitmap bitmap;
    /**
     * 这里测试，直接写死了大小，真正使用过程中，可以提取为自定义属性
     */
    private int mHorizontalPadding = 1;
    private int mVerticalPadding = (getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;

    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;
        mZoomImageView = new ClipZoomImageView(context);
        mClipImageView = new ClipImageBorderView(context);

        android.view.ViewGroup.LayoutParams lp = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        /**
         * 这里，直接写死了图片，
         */
        // mZoomImageView.setImageDrawable(getResources().getDrawable(
        // R.drawable.a));

        this.addView(mZoomImageView, lp);
        this.addView(mClipImageView, lp);

        // 计算padding的px
        mHorizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
                        .getDisplayMetrics());
        mZoomImageView.setHorizontalPadding(mHorizontalPadding);
        mClipImageView.setHorizontalPadding(mHorizontalPadding);
        mZoomImageView.setVerticalPadding(mVerticalPadding);
        mClipImageView.setVerticalPadding(mVerticalPadding);

    }

    public void setImageDrawable(Drawable drawable) {
        mZoomImageView.setImageDrawable(drawable);
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

    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            this.bitmap = bitmap;
            mZoomImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 对外公布设置边距的方法,单位为dp
     *
     * @param mHorizontalPadding
     */
    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }

    /**
     * 设置宽高比例
     *
     * @param widthProportion
     * @param heightProportion
     */
    public void setProportion(int widthProportion, int heightProportion) {
        mClipImageView.setProportion(widthProportion, heightProportion);
        mZoomImageView.setProportion(widthProportion, heightProportion);
    }

    public void setVerticalPadding(int mVerticalPadding) {
        this.mVerticalPadding = mVerticalPadding;
    }

    /**
     * 裁切图片
     *
     * @return
     */
    public Bitmap clip() {
        return mZoomImageView.clip();
    }


    /**
     * 旋转图片
     *
     * @param angle  旋转角度
     * @param bitmap 要处理的Bitmap
     * @return 处理后的Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }


    public void setRotaingImageView(int angle) {

        mZoomImageView.setImageBitmap(adjustPhotoRotation(bitmap, angle));
    }


    public Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();

        try {
            m.setRotate(orientationDegree, (float) bm.getWidth(), (float) bm.getHeight());
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (Exception ex) {
        }

        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bitmap != null){
            bitmap.recycle();
            bitmap = null;
        }
    }
}

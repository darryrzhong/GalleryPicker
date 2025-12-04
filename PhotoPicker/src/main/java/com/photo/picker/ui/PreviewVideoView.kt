package com.maitang.demo

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/11
 * </pre>
 */
class PreviewVideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private var mTextureView: TextureView? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var videoUri: String = ""


    /**
     * 开始播放
     * @param uriString raw or filePath
     * */
    fun startPlay(uriString: String) {
        videoUri = uriString
        initMediaPlayer()
        initTextureView()
        addTextureView()
    }


    /**
     * 结束播放
     * */
    fun stopPlay() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }

    fun release(){
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mTextureView?.let {
            removeView(mTextureView)
            mTextureView = null
        }
    }

    /**
     * 暂停播放
     * */
    fun pausePlay() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    /**
     * 恢复播放
     * */
    fun resumePlay() {
        mMediaPlayer?.start()
    }

    fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    /**
     * 获取当前播放位置
     * */
    fun getCurrentPosition(): Int {
        return mMediaPlayer?.currentPosition ?: 0
    }

    /**
     *到指定时间点位置
     * */
    fun seekTo(msec: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMediaPlayer?.seekTo(msec.toInt())
        }

    }


    /**
     *初始化展示视频内容的TextureView
     * */
    private fun initTextureView() {
        if (mTextureView == null) {
            mTextureView = TextureView(context)
            mTextureView?.surfaceTextureListener = this
        }

    }

    /**
     * 初始化播放器
     * */
    private fun initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setScreenOnWhilePlaying(true)
                setOnPreparedListener(mOnPreparedListener)
                setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
                setOnInfoListener(mOnInfoListener)
                setOnCompletionListener(mOnCompletionListener)
                setOnErrorListener(mOnErrorListener)
            }
        }
    }

    /**
     * 将TextureView添加到容器中
     * */
    private fun addTextureView() {
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT, Gravity.CENTER
        )
        addView(mTextureView, layoutParams)
    }

    /**
     * 调整视频尺寸，宽度铺满，高度适应，居中显示
     * */
    private fun setRenderPrams(mp: MediaPlayer) {
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        val viewWidth = measuredWidth
        val videoRatio = (videoWidth / videoHeight.toFloat())
        val renderHeight = viewWidth / videoRatio
        mTextureView?.let {
            val params = it.layoutParams
            params.height = renderHeight.toInt()
            it.layoutParams = params
        }


    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        // surfaceTexture数据通道准备就绪，开始播放
        openMediaPlayer(surface)
    }


    /**
     * 打开播放器开始加载视频
     * */
    private fun openMediaPlayer(surface: SurfaceTexture?) {
        surface?.let {
            try {
                mMediaPlayer?.apply {
                    reset()
                    isLooping = true
                    setDataSource(context, Uri.parse(videoUri))
                    setSurface(Surface(surface))
                    //异步加载视频
                    prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {

        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }


    private val mOnPreparedListener = MediaPlayer.OnPreparedListener {
        //视频加载完成，开始播放
        setRenderPrams(it)
        it.start()

    }

    private val mOnVideoSizeChangedListener =
        MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->

        }


    private val mOnCompletionListener = MediaPlayer.OnCompletionListener {
        //播放完成

    }

    private val mOnErrorListener = MediaPlayer.OnErrorListener { mp, what, extra ->


        false
    }

    private val mOnInfoListener = MediaPlayer.OnInfoListener { mp, what, extra ->

        true
    }
}
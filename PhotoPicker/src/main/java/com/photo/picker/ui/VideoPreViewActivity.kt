package com.photo.picker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.maitang.demo.PreviewVideoView
import com.photo.picker.R

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/12
 * </pre>
 */
class VideoPreViewActivity : ComponentActivity() {
    private lateinit var videoView: PreviewVideoView
    private lateinit var backView: ImageView
    private lateinit var startView: ImageView
    private var videoPath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_preview)
        handleIntent(intent)
        initView()
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            videoPath = it.getStringExtra("videoPath") ?: ""
        }
    }

    private fun initView() {
        val rootView : View = findViewById(R.id.root_layout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置内边距，避免内容被状态栏遮挡
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
        videoView = findViewById(R.id.video_view)
        backView = findViewById(R.id.player_top_back)
        startView = findViewById(R.id.player_start)
        backView.setOnClickListener { finish() }
        videoView.setOnClickListener {
            startOrPause(videoView.isPlaying())
        }
        if (videoPath.isNotEmpty()) {
            videoView.startPlay(videoPath)
        }
    }

    private fun startOrPause(playing: Boolean) {
        if (playing) {
            startView.visibility = View.VISIBLE
            startView.setImageResource(R.drawable.photo_play)
            videoView.pausePlay()
        } else {
            startView.visibility = View.GONE
            videoView.resumePlay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlay()
    }
}
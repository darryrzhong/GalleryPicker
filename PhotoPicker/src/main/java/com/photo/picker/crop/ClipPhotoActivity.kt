package com.photo.picker.crop

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.photo.picker.GalleryPickerOption
import com.photo.picker.R
import com.photo.picker.utils.BitmapUtils
import com.photo.picker.utils.Utils
import com.photo.picker.utils.getColorFromRes
import com.photo.picker.crop.view.ClipImageLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File


/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/8
 * </pre>
 */
class ClipPhotoActivity : ComponentActivity() {
    private lateinit var mClipImageLayout: ClipImageLayout
    private lateinit var bt_ok: Button
    private lateinit var bt_cancel: RelativeLayout
    private lateinit var rotate_menu: RelativeLayout
    private var imgPath: String = ""
    private var roteNumber = 0
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_clip)
        handleIntent(intent)
        initView()

    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            imgPath = it.getStringExtra("path") ?: ""
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
        mClipImageLayout = findViewById(R.id.id_clipImageLayout)
        bt_ok = findViewById(R.id.bt_photo_ok)
        bt_ok.setTextColor(bt_ok.getColorFromRes(GalleryPickerOption.textColorPrimary))
        bt_ok.background = Utils.createDrawable(
            this,
            bt_ok.getColorFromRes(GalleryPickerOption.colorPrimary),
            60f
        )
        bt_cancel = findViewById(R.id.bt_photo_cancle)
        rotate_menu = findViewById(R.id.rotate_menu)
        mClipImageLayout.setProportion(1, 1);//直接设置比例
        if (imgPath.isNotEmpty()) {
            lifecycleScope.launch {
                BitmapUtils.decodeFile(imgPath)
                    .collect {
                        it.let {
                            mClipImageLayout.setImageBitmap(it)
                        }
                    }
            }
        }
        bt_cancel.setOnClickListener {
            finish()
        }
        bt_ok.setOnClickListener {
            // 剪切图片
            bitmap = mClipImageLayout.clip()
            bitmap?.let {
                lifecycleScope.launch {
                    BitmapUtils.saveBitmapInDisCache(createFilePath(), it).flowOn(Dispatchers.IO)
                        .collect {
                            if (it.isNotEmpty()) {
                                val resultIntent = Intent().apply {
                                    putExtra("result_path", it)
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish() // 结束当前 Activity 并返回结果
                            }
                        }
                }

            }
        }
        rotate_menu.setOnClickListener {
            roteNumber += 90
            mClipImageLayout.setRotaingImageView(roteNumber)
            if (roteNumber == 270) {
                roteNumber = -90
            }
        }
    }

    private fun createFilePath(): String {
        val cacheDir = File(this.cacheDir.absolutePath, "/crop_photo").also { it.mkdirs() }
        val timeStamp = System.currentTimeMillis().toString()
        return "$cacheDir$timeStamp.jpg"
    }

    override fun onDestroy() {
        super.onDestroy()
        recycle();
    }

    private fun recycle() {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        bitmap = null
    }
}
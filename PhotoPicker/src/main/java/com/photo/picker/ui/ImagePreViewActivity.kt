package com.photo.picker.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.photo.picker.R
import com.photo.picker.ui.adapter.ImagePreviewAdapter

/**
 * <pre>
 *     类描述  :
 *
 *
 *     @author : never
 *     @since   : 2024/10/14
 * </pre>
 */
class ImagePreViewActivity : ComponentActivity() {
    private lateinit var vpPage: ViewPager2
    private lateinit var ivBack: ImageView
    private lateinit var tvSelect: TextView
    private var images = ArrayList<String>()
    private var cruPosition = 0
    private lateinit var vpAdapter: ImagePreviewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        handleIntent()
        initView()
    }

    private fun handleIntent() {
        intent.let {
            images = it.getStringArrayListExtra("images") ?: arrayListOf()
            cruPosition = it.getIntExtra("select_position", 0)
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
        vpPage = findViewById(R.id.vp_page)
        ivBack = findViewById(R.id.iv_back)
        tvSelect = findViewById(R.id.tv_sel)
        vpPage.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vpPage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val size = images.size
                tvSelect.text = "${position + 1}/$size"
            }
        })
        vpAdapter = ImagePreviewAdapter(images)
        vpPage.adapter = vpAdapter
        if (cruPosition > 0 && cruPosition < images.size - 1) {
            vpPage.setCurrentItem(cruPosition, false)
        }
        ivBack.setOnClickListener {
            finish()
        }

    }
}
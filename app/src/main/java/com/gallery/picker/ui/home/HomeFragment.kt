package com.gallery.picker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gallery.picker.databinding.FragmentHomeBinding
import com.photo.picker.GalleryPickerHelper
import com.photo.picker.MediaData
import com.photo.picker.MediaType
import com.photo.picker.MimeType
import com.photo.picker.callback.MediaResultCallback
import kotlin.collections.forEach

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initPicker()
        return root
    }

    private fun initPicker() {
        //相册单选
        binding.btnAlbumSingle.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.IMAGE, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                val paths = arrayListOf<String>()
                                paths.add(it.filePath)
                                GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                            }
                        }
                    })
        }
        //相册多选
        binding.btnAlbumMulti.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .maxItems(9)
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.IMAGE, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            if (mediaFiles.isNotEmpty()) {
                                val paths = arrayListOf<String>()
                                mediaFiles.forEach {
                                    paths.add(it.filePath)
                                }
                                GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                            }
                        }
                    })
        }
        //头像裁剪只能单选
        binding.btnAvatarCrop.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .isCrop(true)
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.IMAGE, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                val paths = arrayListOf<String>()
                                paths.add(it.filePath)
                                GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                            }
                        }
                    })
        }
        //视频单选
        binding.btnVideoSingle.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .maxVideoSize(100)
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.VIDEO, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                GalleryPickerHelper.toPreViewVideo(requireActivity(), it.filePath)
                            }
                        }
                    })
        }
        //图片视频混合单选
        binding.btnMixSingle.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.IMAGE_OR_VIDEO, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                if (it.mimeType == MimeType.VIDEO) {
                                    GalleryPickerHelper.toPreViewVideo(requireActivity(), it.filePath)
                                } else if (it.mimeType == MimeType.IMAGE) {
                                    val paths = arrayListOf<String>()
                                    paths.add(it.filePath)
                                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                                }
                            }
                        }
                    })
        }
        //拍照
        binding.btnTakePhoto.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.CAMERA, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                val paths = arrayListOf<String>()
                                paths.add(it.filePath)
                                GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                            }
                        }
                    })
        }
        //拍摄视频
        binding.btnCaptureVideo.setOnClickListener {
            GalleryPickerHelper.newInstance()
                .launchMediaPicker(
                    requireActivity(),
                    MediaType.CAPTURE_VIDEO, object :
                        MediaResultCallback {
                        override fun onMediaResult(mediaFiles: List<MediaData>) {
                            mediaFiles.firstOrNull()?.let {
                                GalleryPickerHelper.toPreViewVideo(requireActivity(), it.filePath)
                            }
                        }
                    })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }
}
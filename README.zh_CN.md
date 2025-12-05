# GalleryPicker

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/darryrzhong/GalleryPicker.svg)](https://jitpack.io/#darryrzhong/GalleryPicker)

<p align="center">
  <img src="./kotlin-picker.gif" alt="picker" width="206" height="443"/>
</p>

GalleryPicker 是一个基于 Android 官方 [Photo Picker API](https://developer.android.google.cn/training/data-storage/shared/photo-picker) 封装的现代图片/视频选择库。它专注于提供**统一、简洁、可兼容多 Android 版本**的媒体选择能力，支持从相册获取图片/视频、拍照、图片裁剪、压缩等功能，且无需获取任何存储权限即可适配 Android 5.0+ 系统。

简体中文 | [English](./README.md)

## 功能特性

* **无需权限**：使用系统标准 Photo Picker，无需申请 `READ_EXTERNAL_STORAGE` 权限（Android 11+）。
* **多版本适配**：
  * **Android 11 (API 30) +**：使用原生 Photo Picker。
  * **Android 4.4 (API 19) - Android 10 (API 29)**：通过 Google Play 服务自动向后移植 Photo Picker，或回退到系统文件选择器 (`ACTION_OPEN_DOCUMENT`)。
* **功能丰富**：
  * 支持单选/多选图片和视频。
  * 支持调用相机拍照和录制视频。
  * 支持图片裁剪（仅限单选）。
  * 支持图片压缩（Luban 算法）。
  * 支持混合单选（图片或视频）。
* **易于使用**：链式调用，回调清晰。

## 接入指南

本文档提供了将 GalleryPicker 库集成到您的 Android 应用程序中的详细指南。

### 1. 添加依赖

在您的项目 `build.gradle` 文件中添加 JitPack 仓库和依赖项。

**根目录 `build.gradle`:**

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**App 模块 `build.gradle`:**

```gradle
dependencies {
    implementation 'com.github.darryrzhong:GalleryPicker:1.0.0' // 请检查最新版本
}
```

### 2. 初始化全局配置 (可选)

你可以在 Application 中进行全局配置，如果不设置则使用默认配置。

```kotlin
GalleryPickerOption.setColorPrimary(R.color.purple_200) // 设置app主题色
    .setTextColorPrimary(R.color.white) // 设置app主题色搭配字体色
    .maxItems(1) // 设置最大选择数量  1 单选  >1多选
    .isCompress(true) // 默认图片压缩
    .ignoreSize(100)  // 小于100kb文件不进行压缩
    .quality(75)  // 压缩比例 默认75%
    .isCrop(false) // 是否进行图片裁剪  默认不裁剪
    .maxVideoSize(15) // 最大选择视频文件大小  默认15Mb
    .debug(false)  // 日志调试
```

### 3. 权限配置

**特别提醒**：使用 Photo Picker 选择媒体文件不需要任何存储权限。但是，如果需要使用**相机拍照**或**录制视频**，虽然 Android 11+ 调用系统相机不需要权限，但如果你的应用清单中声明了相机权限，则必须在运行时获取该权限。

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

### 4. 配置 FileProvider (仅拍照/录像需要)

为了让相机应用能够将拍摄的照片/视频保存到你的应用私有目录，需要配置 `FileProvider`。

1. 在 `AndroidManifest.xml` 中添加 `provider`：

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

2. 在 `res/xml` 目录下创建 `file_paths.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <paths>
        <cache-path
            name="picture_photo"
            path="picture_photo/"/>
    </paths>
</resources>
```

## 进阶使用

使用 `GalleryPickerHelper` 进行媒体选择，无需关心页面跳转及生命周期管理。

### 1. 启动参数配置

启动参数的优先级高于全局默认配置。

```kotlin
GalleryPickerHelper.newInstance()
    .isCompress(true)
    .isCrop(true)
    .ignoreSize(100) // kb
    .quality(75) // 75%
    .maxItems(9) // 单选 or 多选
    .maxVideoSize(15) // 视频文件大小限制 MB
    .launchMediaPicker(this, MediaType.IMAGE, object : MediaResultCallback {
        override fun onMediaResult(mediaFiles: List<MediaData>) {
            // 处理结果
        }
    })
```

### 2. 相册获取 (单选)

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                mediaFiles.firstOrNull()?.let {
                    val paths = arrayListOf<String>()
                    paths.add(it.filePath)
                    // 预览图片
                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                }
            }
        })
```

### 3. 相册获取 (多选)

```kotlin
GalleryPickerHelper.newInstance()
    .maxItems(9)
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE, object : MediaResultCallback {
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
```
<p align="center">
  <img src="./demo.gif" alt="picker" width="206" height="443"/>
</p>


### 4. 相册获取 (带裁剪)

图片裁剪只支持**单选模式**。

```kotlin
GalleryPickerHelper.newInstance()
    .isCrop(true)
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                // 返回的是裁剪后的图片路径
                mediaFiles.firstOrNull()?.let {
                    val paths = arrayListOf<String>()
                    paths.add(it.filePath)
                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                }
            }
        })
```

### 5. 拍照选择

调用拍照前，请确保已处理好相机权限（如果清单文件中声明了）。

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.CAMERA, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                mediaFiles.firstOrNull()?.let {
                    val paths = arrayListOf<String>()
                    paths.add(it.filePath)
                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                }
            }
        })
```

### 6. 视频选择

视频选择支持设置最大文件大小限制（默认 15MB）。目前仅支持单选。

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.VIDEO, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                mediaFiles.firstOrNull()?.let {
                    GalleryPickerHelper.toPreViewVideo(requireActivity(), it.filePath)
                }
            }
        })
```

### 7. 图片 & 视频混合选择

支持同时展示图片和视频，但用户只能选择其中一种类型（单选）。

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE_OR_VIDEO, object : MediaResultCallback {
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
```

### 8. 拍摄视频

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.CAPTURE_VIDEO, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                mediaFiles.firstOrNull()?.let {
                    GalleryPickerHelper.toPreViewVideo(requireActivity(), it.filePath)
                }
            }
        })
```

### 9. 辅助功能

* **图片预览** (仅支持本地路径):

    ```kotlin
    GalleryPickerHelper.toPreViewImage(this, 0, arrayListOf("path1", "path2"))
    ```

* **视频预览** (仅支持本地路径):

    ```kotlin
    GalleryPickerHelper.toPreViewVideo(this, "video_path")
    ```

* **检查设备兼容性**:

    ```kotlin
    GalleryPickerHelper.isPhotoPickerAvailable(this)
    ```

## 设备兼容性说明

照片选择器适用于符合以下条件的设备：

* 搭载 **Android 11 (API 30)** 或更高版本。
* 通过 Google 系统更新接收对模块化系统组件的更改。

对于搭载 **Android 4.4 (API 19) 到 Android 10 (API 29)** 的设备，以及搭载 Android 11 或 12 且支持 Google Play 服务的 Android Go 设备，Google Play 服务会自动安装向后移植的照片选择器模块。

如需通过 Google Play 服务自动安装向后移植的照片选择器模块，请将以下条目添加到应用清单文件的 <application> 标记中:

```xml
<!-- Trigger Google Play services to install the backported photo picker module. -->
<service android:name="com.google.android.gms.metadata.ModuleDependencies"
         android:enabled="false"
         android:exported="false"
         tools:ignore="MissingClass">
    <intent-filter>
        <action android:name="com.google.android.gms.metadata.MODULE_DEPENDENCIES" />
    </intent-filter>
    <meta-data android:name="photopicker_activity:0:required" android:value="" />
</service>
```

如果照片选择器不可用，本库会自动降级调用 `ACTION_OPEN_DOCUMENT` intent，以系统文件管理器的方式进行媒体选择（此时系统会忽略最大选择数量限制）。

## demo
[demo示例](./app-release.apk)

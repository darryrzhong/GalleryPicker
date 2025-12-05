# GalleryPicker

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/darryrzhong/GalleryPicker.svg)](https://jitpack.io/#darryrzhong/GalleryPicker)

<p align="center">
  <img src="./kotlin-picker.gif" alt="picker" width="206" height="443"/>
</p>

GalleryPicker is a modern image/video selection library based on the Android official [Photo Picker API](https://developer.android.google.cn/training/data-storage/shared/photo-picker). It focuses on providing **unified, concise, and multi-version compatible** media selection capabilities, supporting image/video selection from the gallery, taking photos, image cropping, compression, and more, without requiring any storage permissions to adapt to Android 5.0+ systems.

English | [简体中文](./README.zh_CN.md)

## Features

* **No Permissions Required**: Uses the system standard Photo Picker, no need to request `READ_EXTERNAL_STORAGE` permission (Android 11+).
* **Multi-Version Adaptation**:
  * **Android 11 (API 30) +**: Uses the native Photo Picker.
  * **Android 4.4 (API 19) - Android 10 (API 29)**: Automatically backports Photo Picker via Google Play Services, or falls back to the system file picker (`ACTION_OPEN_DOCUMENT`).
* **Rich Functionality**:
  * Supports single/multiple selection of images and videos.
  * Supports taking photos and recording videos via camera.
  * Supports image cropping (single selection only).
  * Supports image compression (Luban algorithm).
  * Supports mixed single selection (image or video).
* **Easy to Use**: Chain calls, clear callbacks.

## Integration Guide

This document provides detailed instructions on how to integrate the GalleryPicker library into your Android application.

### 1. Add Dependency

Add the JitPack repository and dependency to your project's `build.gradle` file.

**Root `build.gradle`:**

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**App Module `build.gradle`:**

```gradle
dependencies {
    implementation 'com.github.darryrzhong:GalleryPicker:1.0.0' // Please check for the latest version
}
```

### 2. Initialize Global Configuration (Optional)

You can configure global settings in your Application. If not set, default configuration will be used.

```kotlin
GalleryPickerOption.setColorPrimary(R.color.purple_200) // Set app primary color
    .setTextColorPrimary(R.color.white) // Set text color for app primary color
    .maxItems(1) // Set max selection count: 1 for single, >1 for multiple
    .isCompress(true) // Default image compression
    .ignoreSize(100)  // Do not compress files smaller than 100kb
    .quality(75)  // Compression quality, default 75%
    .isCrop(false) // Whether to crop image, default false
    .maxVideoSize(15) // Max video file size, default 15Mb
    .debug(false)  // Debug logging
```

### 3. Permission Configuration

**Special Note**: Using Photo Picker to select media files does not require any storage permissions. However, if you need to use **Camera** to take photos or record videos, although Android 11+ invoking system camera doesn't need permission, if your app manifest declares camera permission, you must request it at runtime.

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

### 4. Configure FileProvider (Only for Taking Photos/Videos)

To allow the camera app to save captured photos/videos to your app's private directory, you need to configure `FileProvider`.

1. Add `provider` in `AndroidManifest.xml`:

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

2. Create `file_paths.xml` in `res/xml` directory:

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

## Advanced Usage

Use `GalleryPickerHelper` for media selection without worrying about page transitions and lifecycle management.

### 1. Launch Parameter Configuration

Launch parameter configuration has higher priority than global default configuration.

```kotlin
GalleryPickerHelper.newInstance()
    .isCompress(true)
    .isCrop(true)
    .ignoreSize(100) // kb
    .quality(75) // 75%
    .maxItems(9) // Single or Multiple
    .maxVideoSize(15) // Video file size limit in MB
    .launchMediaPicker(this, MediaType.IMAGE, object : MediaResultCallback {
        override fun onMediaResult(mediaFiles: List<MediaData>) {
            // Handle result
        }
    })
```

### 2. Gallery Selection (Single)

```kotlin
GalleryPickerHelper.newInstance()
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                mediaFiles.firstOrNull()?.let {
                    val paths = arrayListOf<String>()
                    paths.add(it.filePath)
                    // Preview image
                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                }
            }
        })
```

### 3. Gallery Selection (Multiple)

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


### 4. Gallery Selection (With Crop)

Image cropping only supports **Single Selection Mode**.

```kotlin
GalleryPickerHelper.newInstance()
    .isCrop(true)
    .launchMediaPicker(
        requireActivity(),
        MediaType.IMAGE, object : MediaResultCallback {
            override fun onMediaResult(mediaFiles: List<MediaData>) {
                // Returns the cropped image path
                mediaFiles.firstOrNull()?.let {
                    val paths = arrayListOf<String>()
                    paths.add(it.filePath)
                    GalleryPickerHelper.toPreViewImage(requireActivity(), 0, paths)
                }
            }
        })
```

### 5. Take Photo

Before calling take photo, ensure camera permission is handled (if declared in manifest).

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

### 6. Video Selection

Video selection supports setting max file size limit (default 15MB). Currently only supports single selection.

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

### 7. Mixed Selection (Image & Video)

Supports displaying both images and videos, but user can only select one type (Single Selection).

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

### 8. Record Video

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

### 9. Helper Functions

* **Image Preview** (Local paths only):

    ```kotlin
    GalleryPickerHelper.toPreViewImage(this, 0, arrayListOf("path1", "path2"))
    ```

* **Video Preview** (Local paths only):

    ```kotlin
    GalleryPickerHelper.toPreViewVideo(this, "video_path")
    ```

* **Check Device Compatibility**:

    ```kotlin
    GalleryPickerHelper.isPhotoPickerAvailable(this)
    ```

## Device Compatibility

Photo Picker is available on devices that meet the following criteria:

* Running **Android 11 (API 30)** or higher.
* Receives changes to Modular System Components via Google System Updates.

For devices running **Android 4.4 (API 19) to Android 10 (API 29)**, and Android Go devices running Android 11 or 12 that support Google Play Services, Google Play Services will automatically install the backported Photo Picker module.

To trigger Google Play services to install the backported photo picker module, add the following entry to the `<application>` tag in your app manifest:

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

If Photo Picker is unavailable, this library will automatically degrade to call `ACTION_OPEN_DOCUMENT` intent, using the system file manager for media selection (in this case, the system ignores the maximum selection count limit).

## demo
[demo example](./app-release.apk)
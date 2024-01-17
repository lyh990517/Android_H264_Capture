package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class CaptureManager(private val context: Context) {
    private val captureThread = HandlerThread("captureThread")
    private val mediaProjectionManager: MediaProjectionManager =
        (context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
    private val imageReader: ImageReader = ImageReader.newInstance(
        context.resources.displayMetrics.widthPixels,
        context.resources.displayMetrics.heightPixels,
        PixelFormat.RGBA_8888,
        2
    )

    init {
        captureThread.start()
    }

    fun startCapture(code: Int, data: Intent) {
        mediaProjectionManager.getMediaProjection(code, data).createVirtualDisplay(
            "ScreenCapture",
            context.resources.displayMetrics.widthPixels,
            context.resources.displayMetrics.heightPixels,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        imageReader.setOnImageAvailableListener({
            val image = it.acquireLatestImage()
            Log.e("CaptureImage", "$image")
            image?.close()
        }, Handler(captureThread.looper))
    }

    fun stopCapture() {
        captureThread.quitSafely()
    }

}
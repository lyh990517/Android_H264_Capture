package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi

object CaptureManager {
    private lateinit var mediaProjection: MediaProjection
    private var h264Encoder: H264Encoder? = null
    fun initialize(code: Int, data: Intent, context: Context) {
        val mediaProjectionManager: MediaProjectionManager =
            (context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
        val window by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
        val display = context.resources.displayMetrics
        mediaProjectionManager.apply {
            mediaProjection = getMediaProjection(code, data)
            h264Encoder = H264Encoder(
                width = window.currentWindowMetrics.bounds.width(),
                height = window.currentWindowMetrics.bounds.height(),
                bindSurface = { surface ->
                    mediaProjection.createVirtualDisplay(
                        "screen-h264",
                        display.widthPixels,
                        display.heightPixels,
                        display.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        surface,
                        null,
                        null
                    )
                }
            )
        }
    }

    fun startCapture() {
        h264Encoder?.startEncode()
    }

    fun stopCapture() {
        h264Encoder?.stopEncode()
        mediaProjection.stop()
    }
}


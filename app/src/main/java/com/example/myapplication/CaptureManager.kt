package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.view.WindowManager

object CaptureManager {
    private lateinit var mediaProjection: MediaProjection
    private var h264Encoder: H264Encoder? = null
    fun initialize(filePath: String, code: Int, data: Intent, context: Context) {
        val mediaProjectionManager: MediaProjectionManager =
            (context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
        val window by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
        val display = context.resources.displayMetrics
        mediaProjectionManager.apply {
            mediaProjection = getMediaProjection(code, data)
            h264Encoder = H264Encoder(
                filePath = filePath,
                width = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.currentWindowMetrics.bounds.width() else window.defaultDisplay.width,
                height = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) window.currentWindowMetrics.bounds.height() else window.defaultDisplay.height,
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


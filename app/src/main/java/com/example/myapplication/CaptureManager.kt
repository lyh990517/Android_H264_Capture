package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager

class CaptureManager(private val context: Context) {
    private val mediaProjectionManager: MediaProjectionManager =
        (context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
    private lateinit var mediaProjection: MediaProjection
    private var h264Encoder: H264EncodeThread? = null

    fun initialize(code: Int, data: Intent) {
        val display = context.resources.displayMetrics
        mediaProjectionManager.apply {
            mediaProjection = getMediaProjection(code, data)
            h264Encoder = H264EncodeThread(
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

    }

}
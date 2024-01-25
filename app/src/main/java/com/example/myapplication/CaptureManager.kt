package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.example.screen_capture.H264EncoderCaptureFactory

object CaptureManager {
    private val captureManager = H264EncoderCaptureFactory.create()
    fun startCapture(filePath: String, code: Int, data: Intent, context: Context) {
        captureManager.startCapture(filePath, code, data, context)
    }

    fun stopCapture() {
        captureManager.stopCapture()
    }
}


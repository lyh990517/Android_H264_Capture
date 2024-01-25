package com.example.screen_capture

import android.content.Context
import android.content.Intent

interface H264EncoderCapture {
    fun startCapture(filePath: String, code: Int, data: Intent, context: Context)

    fun stopCapture()
}
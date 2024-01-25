package com.example.screen_capture

object H264EncoderCaptureFactory {
    fun create() : H264EncoderCapture = H264EncoderCaptureImpl()
}
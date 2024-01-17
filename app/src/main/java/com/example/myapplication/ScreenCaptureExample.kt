package com.example.myapplication

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.util.Log
import java.nio.ByteBuffer

class ScreenCaptureExample(private val context: Context) {
    private var mediaProjection: MediaProjection? = null
    private var mediaCodec: MediaCodec? = null

    fun startCapture(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection

        val metrics = DisplayMetrics()
        val format = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            metrics.widthPixels,
            metrics.heightPixels
        )
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        format.setInteger(MediaFormat.KEY_BIT_RATE, 125000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        try {
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec?.createInputSurface()
            mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                metrics.widthPixels, metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null
            )
            mediaCodec?.start()
            captureLoop()
        } catch (e: Throwable) {
            Log.e("error", e.message ?: "")
        }
    }

    private fun captureLoop() {
        val bufferInfo = MediaCodec.BufferInfo()
        while (true) { // 루프를 통한 화면 캡처 및 인코딩
            val outputBufferIndex = mediaCodec?.dequeueOutputBuffer(bufferInfo, 10000)
                ?: MediaCodec.INFO_TRY_AGAIN_LATER
            if (outputBufferIndex >= 0) {
                // 인코딩된 데이터 처리
                val outputBuffer = mediaCodec?.getOutputBuffer(outputBufferIndex)
                outputBuffer?.let { processEncodedData(it, bufferInfo) }

                mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    private fun processEncodedData(encodedData: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        Log.e("processEncodedData", "${encodedData.get()} ${bufferInfo.size}")
    }

}
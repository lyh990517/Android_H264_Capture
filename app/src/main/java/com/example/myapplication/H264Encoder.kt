package com.example.myapplication

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.flow.MutableSharedFlow

class H264Encoder(
    private val context: Context,
    private val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
    private val bindSurface: (Surface) -> Unit,
) : Thread("H264-Thread") {
    private val mediaCodec: MediaCodec =
        MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    private val width = context.resources.displayMetrics.widthPixels
    private val height = context.resources.displayMetrics.heightPixels
    private val outputStream = MutableSharedFlow<ByteArray>()

    init {
        initMediaCodec()
    }

    private fun initMediaCodec() {
        val mediaFormat =
            MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                width,
                height
            )
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)

        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height)

        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            colorFormat
        )


        mediaCodec.configure(
            mediaFormat,
            null,
            null,
            MediaCodec.CONFIGURE_FLAG_ENCODE
        )

        bindSurface.let {
            //录屏和编码关联
            val surface = mediaCodec.createInputSurface()
            it(surface)
        }
    }

    fun encode() {
        mediaCodec.start()
    }

    override fun run() {
        super.run()
        Log.e("H264","Start!")
    }
}
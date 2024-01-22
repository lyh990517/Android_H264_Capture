package com.example.myapplication

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

class H264Encoder(
    private val width: Int = 720,
    private val height: Int = 1080,
    private val bindSurface: ((Surface) -> Unit)? = null,
    private val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
) : Thread(("encode-h264")) {
    private val mediaCodec: MediaCodec =
        MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

    init {
        initMediaCodec()
    }

    private fun initMediaCodec() {
        val mediaFormat =
            MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                width,
                height)
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

        bindSurface?.let {
            val surface = mediaCodec.createInputSurface()
            it(surface)
        }
    }

    @Volatile
    private var isStop = true

    override fun run() {
        super.run()
        mediaCodec.start()
        val info: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
        try {
            while (!isStop) {
                var outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                while (outIndex >= 0) {
                    val outputBuffer = mediaCodec.getOutputBuffer(outIndex)
                    val data = ByteArray(info.size)
                    outputBuffer?.get(data)
                    Log.e("data","${data.size}")

                    mediaCodec.releaseOutputBuffer(
                        outIndex,
                        false
                    )
                    outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                }

            }
        } catch (e: Exception) {

            e.printStackTrace()
            onException?.apply {
                invoke(e.message ?: "")
            }
        } finally {
            isStop = true
            mediaCodec.stop()
            mediaCodec.release()

        }
    }

    fun startEncode() {
        isStop = false
        start()
    }

    fun stopEncode() {
        isStop = true
    }

    var onException: ((String) -> Unit)? = null

}
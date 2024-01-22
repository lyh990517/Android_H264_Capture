package com.example.myapplication

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

class H264Encoder(
    private val WIDTH: Int = 720,
    private val HEIGHT: Int = 1080,
    var encode: (() -> ByteArray)? = null,
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
                WIDTH,
                HEIGHT)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, WIDTH * HEIGHT)

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

                encode?.apply {
                    val temp = invoke()
                    val inIndex = mediaCodec.dequeueInputBuffer(100_000)
                    if (inIndex >= 0) {
                        val byteBuffer: ByteBuffer? = mediaCodec.getInputBuffer(inIndex)
                        byteBuffer?.clear()
                        byteBuffer?.put(temp)
                        mediaCodec.queueInputBuffer(
                            inIndex,
                            0,
                            temp.size,
                            System.nanoTime() / 1000,
                            0)
                    }
                }


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
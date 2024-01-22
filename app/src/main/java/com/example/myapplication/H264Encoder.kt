package com.example.myapplication

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import android.view.Surface
import java.io.File

class H264Encoder(
    private val filePath: String,
    private val width: Int,
    private val height: Int,
    private val bindSurface: ((Surface) -> Unit)? = null,
    private val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
) : Thread(("encode-h264")) {
    private val mediaCodec: MediaCodec =
        MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    private lateinit var mediaMuxer: MediaMuxer
    private var trackIndex = -1
    private var muxerStarted = false


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
        val file = File(filePath)
        file.parentFile?.apply { if (!exists()) mkdir() }
        if (!file.exists()) file.createNewFile()
        mediaMuxer = MediaMuxer(file.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        try {
            while (!isStop) {
                var outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                while (outIndex >= 0) {
                    val outputBuffer = mediaCodec.getOutputBuffer(outIndex)
                    if (!muxerStarted) {
                        val mediaFormat = mediaCodec.outputFormat
                        trackIndex = mediaMuxer.addTrack(mediaFormat)
                        mediaMuxer.start()
                        muxerStarted = true
                    }
                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        info.size = 0
                    }
                    if (info.size != 0) {
                        if (muxerStarted) {
                            outputBuffer?.position(info.offset)
                            outputBuffer?.limit(info.offset + info.size)
                            mediaMuxer.writeSampleData(trackIndex, outputBuffer!!, info)
                        }
                    }

                    mediaCodec.releaseOutputBuffer(outIndex, false)
                    outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                }

            }
        } catch (e: Exception) {

            e.printStackTrace()
            onException?.apply {
                invoke(e.message ?: "")
            }
        } finally {
            if (muxerStarted) {
                mediaMuxer.stop()
                mediaMuxer.release()
            }
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
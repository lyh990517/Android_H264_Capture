package com.example.screen_capture

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

internal class H264Encoder(
    private val filePath: String,
    private val width: Int,
    private val height: Int,
    private val bindSurface: ((Surface) -> Unit)? = null,
    private val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
) {

    private val h264Scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    private lateinit var mediaMuxer: MediaMuxer
    private var trackIndex = -1
    private var muxerStarted = false

    @Volatile
    private var isStop = true

    init {
        initMediaCodec()
    }

    private fun initMediaCodec() {
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
            setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        bindSurface?.invoke(mediaCodec.createInputSurface())
    }

    private fun runEncodingProcess() {
        mediaCodec.start()
        val info = MediaCodec.BufferInfo()
        prepareFile(filePath).let { mediaMuxer = MediaMuxer(it, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) }

        try {
            while (!isStop && h264Scope.coroutineContext.job.isActive) {
                encodeVideo(info)
            }
        } catch (e: Exception) {
            // 예외 처리
        } finally {
            tearDown()
        }
    }

    private fun encodeVideo(info: MediaCodec.BufferInfo) {
        var outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
        while (outIndex >= 0) {
            mediaCodec.getOutputBuffer(outIndex)?.let { outputBuffer ->
                startMuxerIfNeeded(info)
                writeEncodedDataToMuxer(outputBuffer, info)
                mediaCodec.releaseOutputBuffer(outIndex, false)
            }
            outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
        }
    }

    private fun prepareFile(filePath: String): String {
        return File(filePath).apply {
            parentFile?.takeIf { !it.exists() }?.mkdirs()
            if (!exists()) createNewFile()
        }.path
    }

    private fun startMuxerIfNeeded(info: MediaCodec.BufferInfo) {
        if (!muxerStarted && info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            trackIndex = mediaMuxer.addTrack(mediaCodec.outputFormat).also { _ ->
                mediaMuxer.start()
                muxerStarted = true
            }
        }
    }

    private fun writeEncodedDataToMuxer(outputBuffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        if (info.size != 0 && muxerStarted) {
            outputBuffer.position(info.offset).limit(info.offset + info.size)
            mediaMuxer.writeSampleData(trackIndex, outputBuffer, info)
        }
    }

    private fun tearDown() {
        if (muxerStarted) {
            mediaMuxer.stop()
            mediaMuxer.release()
        }
        mediaCodec.stop()
        mediaCodec.release()
    }

    fun startEncode() {
        isStop = false
        h264Scope.launch { // 코루틴으로 비동기 작업 시작
            runEncodingProcess()
        }
    }

    fun stopEncode() {
        isStop = true
        h264Scope.coroutineContext.job.cancelChildren() // 코루틴 작업 취소
    }

}

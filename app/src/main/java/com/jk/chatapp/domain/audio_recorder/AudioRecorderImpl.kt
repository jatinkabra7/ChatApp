package com.jk.chatapp.domain.audio_recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AudioRecorderImpl(
    private val context: Context
) : AudioRecorder {

    private var recorder : MediaRecorder? = null

    private fun createRecorder() : MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun startAudioRecording(outputFile: File) {

        createRecorder().apply {

            setAudioSource(MediaRecorder.AudioSource.MIC)

            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()

            start()

            recorder = this
        }
    }

    override fun stopAudioRecording() {

        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}
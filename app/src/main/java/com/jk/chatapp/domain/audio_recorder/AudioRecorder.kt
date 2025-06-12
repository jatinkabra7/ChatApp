package com.jk.chatapp.domain.audio_recorder

import java.io.File

interface AudioRecorder {
    fun startAudioRecording(outputFile : File)
    fun stopAudioRecording()
}
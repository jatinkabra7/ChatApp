package com.jk.chatapp.domain.audio_player

interface AudioPlayer {
    fun play(url : String)
    fun pause()
    fun isPlaying() : Boolean
    fun setOnCompleteListener(listener : () -> Unit)
    fun progress() : Long
    fun duration() : Long
}
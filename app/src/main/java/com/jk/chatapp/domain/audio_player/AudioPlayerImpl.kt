package com.jk.chatapp.domain.audio_player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayerImpl(
    private val exoPlayer: ExoPlayer
) : AudioPlayer {

    override fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }

    override fun setOnCompleteListener(listener: () -> Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED) {
                    listener.invoke()
                }
            }
        })
    }

    override fun progress(): Long {
        return exoPlayer.currentPosition
    }

    override fun duration(): Long {
        return exoPlayer.duration
    }
}
package com.reproductor.barkbeat

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.reproductor.barkbeat.models.SongModel


object MyExoplayer {

    private var exoPlayer : ExoPlayer? = null
    private var currentSong : SongModel? =null
    private var songList: MutableList<SongModel> = mutableListOf()

    fun getCurrentSong() : SongModel?{
        return currentSong
    }

    fun getSongList(): List<SongModel> {
        return songList
    }

    fun getInstance() : ExoPlayer?{
        return exoPlayer
    }

    fun startPlaying(context : Context, song : SongModel){
        if(exoPlayer==null)
            exoPlayer = ExoPlayer.Builder(context).build()

        if(currentSong!=song){
            currentSong = song

            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()

            }
        }


    }



}
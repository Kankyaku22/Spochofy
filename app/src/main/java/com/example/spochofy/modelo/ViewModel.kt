package com.example.spochofy.modelo

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.AnyRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.spochofy.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReproductorModel: ViewModel() {


    private val canciones = listOf<Musica>(
        Musica("Balls to the wall", R.drawable.accept,"Accept","Balls to the wall",R.raw.accept),
        Musica("Earthrise",R.drawable.camel,"Camel","Mirage",R.raw.earthrise),
        Musica("The Final Countdown",R.drawable.europe,"The Final Countdown","",R.raw.europe),
        Musica("Caroline",R.drawable.hello,"Status Quo","Hello!",R.raw.caroline),
        Musica("Sultans Of Swing",R.drawable.direstraits,"Dire Straits","DireStraits",R.raw.sultansofswing)
    )

    private val MusicaShuffled = canciones.shuffled()


    private val _index = MutableStateFlow(0)

    val index = _index.asStateFlow()

    private val _exoPlayer: MutableStateFlow<ExoPlayer?> = MutableStateFlow(null)
    val exoPlayer = _exoPlayer.asStateFlow()


    private var _currentSong = MutableStateFlow(canciones[index.value])
    val currentSong = _currentSong.asStateFlow()


    private var _duracion = MutableStateFlow(0)
    val duracion = _duracion.asStateFlow()


    private var _progreso = MutableStateFlow(0)
    val progreso = _progreso.asStateFlow()


    private var _isRepeating = MutableStateFlow(false)
    val isRepeating = _isRepeating.asStateFlow()


    private var _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()


    private var _isPlaying = MutableStateFlow(true)
    var isPlaying = _isPlaying.asStateFlow()


    fun createExoPlayer(context: Context){
        _exoPlayer.value = ExoPlayer.Builder(context).build()
        exoPlayer.value!!.prepare()
    }
    fun playSong(context: Context){
        val item = MediaItem.fromUri(obtenerRuta(context, currentSong.value.media))
        exoPlayer.value!!.setMediaItem(item)
        exoPlayer.value!!.play()
        exoPlayer.value!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _duracion.value = _exoPlayer.value!!.duration.toInt()
                        viewModelScope.launch {
                            while (isActive) {
                                _progreso.value = _exoPlayer.value!!.currentPosition.toInt()
                                delay(1000)
                                if (_exoPlayer.value!!.currentPosition >= _exoPlayer.value!!.duration && isRepeating.value) {
                                    _exoPlayer.value!!.seekTo(0)
                                }
                            }
                        }
                    }
                    Player.STATE_ENDED -> {
                        if (isRepeating.value) {
                            _exoPlayer.value!!.seekTo(0)
                            _exoPlayer.value!!.play()
                        } else {
                            nextSong(context)
                        }
                    }
                }
            }
        })
    }

    fun changeRepeatingState(newState: Boolean){
        _isRepeating.value = newState
    }

    fun changeShuffleState(context: Context, newState: Boolean){
        _isShuffle.value = newState
        if(_isShuffle.value) _index.value = 0
    }




    override fun onCleared() {
        _exoPlayer.value!!.release()
        super.onCleared()
    }

    fun nextSong(context: Context){
        if(_index.value==canciones.size-1 && isRepeating.value){
            _index.value = 0
            if(_isShuffle.value) _currentSong.value = MusicaShuffled[_index.value]
            else _currentSong.value = canciones[_index.value]
            CambiarCancion(context)
        }
        else if(_index.value < canciones.size-1){
            _index.value += 1
            if(_isShuffle.value) _currentSong.value = MusicaShuffled[_index.value]
            else _currentSong.value = canciones[_index.value]
            CambiarCancion(context)
        }
    }

    fun playPause() {
        if (_exoPlayer.value!!.isPlaying){
            _exoPlayer.value!!.pause()
            _isPlaying.value = false
        }else {
            _exoPlayer.value!!.play()
            _isPlaying.value = true
        }
    }

    fun CambiarCancion(context: Context) {
        _exoPlayer.value!!.stop()
        _exoPlayer.value!!.clearMediaItems()
        _exoPlayer.value!!.setMediaItem(MediaItem.fromUri(obtenerRuta(context, _currentSong.value.media)))
        _exoPlayer.value!!.prepare()
        _exoPlayer.value!!.playWhenReady = true
    }



    fun previa(context: Context){
        if(_index.value==0 && isRepeating.value){
            _index.value = canciones.size-1
            if(_isShuffle.value) _currentSong.value = MusicaShuffled[_index.value]
            else _currentSong.value = canciones[_index.value]
            CambiarCancion(context)
        }
        else if(_index.value > 0){
            _index.value -= 1
            if(_isShuffle.value) _currentSong.value = MusicaShuffled[_index.value]
            else _currentSong.value = canciones[_index.value]
            CambiarCancion(context)
        }
    }

    fun camProgreso(progreso: Int){
        _exoPlayer.value!!.seekTo(progreso.toLong())
    }


    @Throws(Resources.NotFoundException::class)
    fun obtenerRuta(context: Context, @AnyRes resId: Int): Uri {
        val res: Resources = context.resources
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + res.getResourcePackageName(resId)
                    + '/' + res.getResourceTypeName(resId)
                    + '/' + res.getResourceEntryName(resId)
        )
    }
}


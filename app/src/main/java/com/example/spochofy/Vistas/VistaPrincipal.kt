package com.example.spochofy.Vistas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.Center
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.spochofy.modelo.Musica
import com.example.spochofy.modelo.ReproductorModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spochofy.R
@Preview
@Composable
fun PantallaPrincipal(){
    val model : ReproductorModel = viewModel()
    val cancionActual = model.currentSong.collectAsState().value
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF191414))
    ){

        SongInfoText(cancionActual)
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .align(CenterHorizontally)
        ){
            Image(
                modifier = Modifier
                    .size(400.dp),
                painter = painterResource(id = R.drawable.vinyl_background),
                contentDescription = "vinyl background"
            )

            Image(painter = painterResource(id = cancionActual.portada),
                contentDescription = "",
                Modifier
                    .fillMaxSize(0.5f)
                    .aspectRatio(1.0f)
                    .align(Alignment.Center)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,)

        }
        PlayerControls(model, cancionActual)
        }
}

@Composable
fun SongInfoText(cancionActual: Musica){
    Column (
        Modifier
            .fillMaxWidth()
            .padding(10.dp)){
        Text(text = "Now playing", fontSize = 25.sp, color = Color.White)
        Text(text = cancionActual.nombre + " - " + cancionActual.artista, fontSize = 25.sp, color = Color.White)
    }
}


@Composable
fun PlayerControls(model: ReproductorModel, cancionActual: Musica){

    val context = LocalContext.current
    val isPlaying = model.isPlaying.collectAsState()
    val isShuffle = model.isShuffle.collectAsState()
    val isRepeat = model.isRepeating.collectAsState()
    var playIcon = R.drawable.pausa
    var repeatIcon = R.drawable.repetir
    var shuffleIcon = R.drawable.random

    if(isRepeat.value) repeatIcon = R.drawable.repetiractivado
    if(isShuffle.value) shuffleIcon = R.drawable.randomactivado
    if(!isPlaying.value) playIcon = R.drawable.play
    LaunchedEffect(Unit){
        model.createExoPlayer(context)
        model.playSong(context)
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            SliderView(cancionActual)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { model.changeShuffleState(context,!isShuffle.value) }){
                    Image(painter = painterResource(id = shuffleIcon), contentDescription = "")
                }
                TextButton(onClick = { model.previa(context) }){
                    Icon(Icons.Default.ArrowBack, contentDescription ="",modifier = Modifier.size(20.dp))
                }
                Button(onClick = { model.playPause() }, modifier = Modifier
                    .size(70.dp)
                    .clip(
                        CircleShape
                    ),colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1db954))
                ){
                    Image(painter = painterResource(id = playIcon), contentDescription = "", modifier = Modifier.fillMaxSize()
                        )
                }
                TextButton(onClick = { model.nextSong(context) }){
                    Icon(Icons.Default.ArrowForward, contentDescription ="",modifier = Modifier.size(20.dp))
                }
                TextButton(onClick = {  model.changeRepeatingState(!isRepeat.value) }){
                    Image(painter = painterResource(id = repeatIcon), contentDescription = "")
                }
            }

        }
        UIControls()
    }
}

fun durationParsed(tiempo: Int): String{
    val minutos = tiempo/60
    val segundos = tiempo - (minutos*60)
    var minutosString = "" + minutos
    var segundosString = "" + segundos
    if(segundos<10) segundosString = "0$segundosString"
    if(minutos<10) minutosString = "0$minutosString"
    return "$minutosString:$segundosString"
}
@Composable
fun UIControls(){
    Row(
        Modifier
            .background(Color.DarkGray)
            .height(80.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { /*TODO*/ }){
            Image(painter = painterResource(id = R.drawable.casa), contentDescription = "")
        }
        TextButton(onClick = { /*TODO*/ }){
            Image(painter = painterResource(id = R.drawable.lupa), contentDescription = "")
        }
    }
}




@Composable
fun SliderView(cancionActual: Musica){
    val model: ReproductorModel =viewModel()
    val duracion = model.duracion.collectAsState()
    val progreso = model.progreso.collectAsState()

    Column(
        Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Slider(value = progreso.value.toFloat(), onValueChange = { model.camProgreso(it.toInt())}, valueRange = 0f..duracion.value.toFloat(),
            colors = SliderDefaults.colors(thumbColor = Color(0xFF1db954)))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = durationParsed((progreso.value/1000)), color = Color.White)
            Text(text = durationParsed(duracion.value/1000), color = Color.White)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PantallaPrincipalPreview(){
    PantallaPrincipal()
}
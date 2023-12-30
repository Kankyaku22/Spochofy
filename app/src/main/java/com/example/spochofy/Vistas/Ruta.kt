package com.example.spochofy.Vistas

sealed class Ruta(var ruta: String) {
    object VistaPrincipal: Ruta(ruta = "pantallaprincipal")
}
package com.reproductor.barkbeat.models

data class PlaylistModel(
    var id: String = "",
    val nombre: String = "",
    val usuarioId: String = "",
    val fechaCreacion: Long = 0
) {

    constructor() : this("", "", "", 0)
}

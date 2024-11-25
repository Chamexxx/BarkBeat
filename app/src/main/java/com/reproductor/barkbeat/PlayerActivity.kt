package com.reproductor.barkbeat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.reproductor.barkbeat.databinding.ActivityPlayerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import com.reproductor.barkbeat.models.SongModel

class PlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar ExoPlayer y cargar la canción actual
        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text = subtitle
            Glide.with(binding.songCoverImageView).load(coverUrl)
                .circleCrop()
                .into(binding.songCoverImageView)

            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer
        }

        // Configurar el botón para agregar la canción a una playlist
        binding.buttonAgregarAPlaylist.setOnClickListener {
            mostrarDialogoSeleccionarPlaylist()
        }
    }

    private fun mostrarDialogoSeleccionarPlaylist() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("playlists").whereEqualTo("usuarioId", userId).get()
                .addOnSuccessListener { documents ->
                    val playlistNames = mutableListOf<String>()
                    val playlistIds = mutableListOf<String>()

                    for (document in documents) {
                        playlistNames.add(document.getString("nombre") ?: "Sin nombre")
                        playlistIds.add(document.id)
                    }

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Seleccionar Playlist")
                    builder.setItems(playlistNames.toTypedArray()) { _, which ->
                        val selectedPlaylistId = playlistIds[which]
                        verificarYAgregarCancionAPlaylist(selectedPlaylistId)
                    }
                    builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
                    builder.show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar las playlists: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarYAgregarCancionAPlaylist(playlistId: String) {
        val currentSong = MyExoplayer.getCurrentSong()

        if (currentSong != null) {
            val songId = currentSong.id // Usar el ID de la canción

            // Verificar si la canción ya existe en la subcolección "songs"
            db.collection("playlists").document(playlistId).collection("songs").document(songId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // La canción ya existe en la playlist
                        mostrarDialogoCancionExistente(playlistId, currentSong)
                    } else {
                        // La canción no existe, agregarla
                        agregarCancionAPlaylist(playlistId, currentSong)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al verificar la canción: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No hay ninguna canción en reproducción", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoCancionExistente(playlistId: String, currentSong: SongModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Canción ya existente")
        builder.setMessage("Esta canción ya está en la playlist seleccionada. ¿Deseas volver a añadirla?")

        builder.setPositiveButton("Añadir de nuevo") { _, _ ->
            agregarCancionAPlaylist(playlistId, currentSong)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun agregarCancionAPlaylist(playlistId: String, currentSong: SongModel) {
        val songId = currentSong.id // Usar el ID de la canción
        val songData = hashMapOf(
            "title" to currentSong.title,
            "subtitle" to currentSong.subtitle,
            "url" to currentSong.url,
            "coverUrl" to currentSong.coverUrl
        )

        // Guardar la canción en la playlist con su ID como clave
        db.collection("playlists").document(playlistId).collection("songs").document(songId)
            .set(songData) // Usamos set en lugar de add para definir manualmente el ID
            .addOnSuccessListener {
                Toast.makeText(this, "Canción añadida a la playlist", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al añadir la canción: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

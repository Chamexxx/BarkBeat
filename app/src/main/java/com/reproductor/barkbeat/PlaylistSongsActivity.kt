package com.reproductor.barkbeat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.adapters.SongListAdapter
import com.reproductor.barkbeat.databinding.ActivityPlaylistSongsBinding
import com.reproductor.barkbeat.models.SongModel

class PlaylistSongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistSongsBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var songListAdapter: SongListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el RecyclerView con SongListAdapter
        songListAdapter = SongListAdapter(mutableListOf()) // Inicializar con una lista vacía
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistSongsActivity)
            adapter = songListAdapter
        }

        // Obtener el ID de la playlist seleccionada
        val playlistId = intent.getStringExtra("playlistId")
        if (playlistId != null) {
            cargarCanciones(playlistId)
        } else {
            Log.e("PlaylistSongsActivity", "Playlist ID no proporcionado")
            finish()
        }
    }

    private fun cargarCanciones(playlistId: String) {
        db.collection("playlists").document(playlistId).collection("songs")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.w("PlaylistSongsActivity", "No se encontraron canciones en la playlist: $playlistId")
                    return@addOnSuccessListener
                }

                // Mapeo de IDs de las canciones
                val songIds = result.documents.mapNotNull { it.id }

                if (songIds.isEmpty()) {
                    Log.w("PlaylistSongsActivity", "No se encontraron IDs válidos en la subcolección songs.")
                    return@addOnSuccessListener
                }

                Log.d("PlaylistSongsActivity", "Actualizando adaptador con canciones: $songIds")
                songListAdapter.updateSongs(songIds)
            }
            .addOnFailureListener { exception ->
                Log.e("PlaylistSongsActivity", "Error obteniendo canciones: ${exception.message}")
            }
    }

}
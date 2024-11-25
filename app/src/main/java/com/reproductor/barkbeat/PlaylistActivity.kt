package com.reproductor.barkbeat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.adapters.PlaylistAdapter
import com.reproductor.barkbeat.databinding.ActivityPlaylistBinding
import com.reproductor.barkbeat.models.PlaylistModel

class PlaylistActivity : AppCompatActivity(), PlaylistAdapter.OnPlaylistClickListener {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistAdapter: PlaylistAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener para manejar los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar RecyclerView con el adaptador modificado
        playlistAdapter = PlaylistAdapter(this, this) // Pasar el contexto y el listener
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = playlistAdapter
        }
        cargarPlaylists()

        // Configurar la barra de navegación inferior
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_library -> {
                    // Ya estamos en PlaylistActivity, no es necesario hacer nada
                    true
                }
                else -> false
            }
        }
        // Seleccionar el ítem actual para esta actividad
        bottomNavigationView.selectedItemId = R.id.nav_library
    }

    private fun cargarPlaylists() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PlaylistActivity", "Usuario no autenticado")
            return
        }

        db.collection("playlists")
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { result ->
                // Crear la lista de playlists asignando manualmente el ID del documento
                val playlists = result.documents.mapNotNull { document ->
                    val playlist = document.toObject(PlaylistModel::class.java)
                    playlist?.apply {
                        id = document.id // Asignar manualmente el ID del documento
                    }
                }
                Log.d("PlaylistActivity", "Playlists cargadas: $playlists")
                playlistAdapter.setPlaylists(playlists) // Actualizar adaptador con los datos obtenidos
            }
            .addOnFailureListener { exception ->
                Log.e("PlaylistActivity", "Error obteniendo playlists", exception)
            }
    }

    override fun onPlaylistClick(playlistId: String) {
        if (playlistId.isEmpty()) {
            Log.e("PlaylistActivity", "El ID de la playlist es vacío o nulo")
            return
        }

        Log.d("PlaylistActivity", "Navegando a PlaylistSongsActivity con ID: $playlistId")
        val intent = Intent(this, PlaylistSongsActivity::class.java)
        intent.putExtra("playlistId", playlistId) // Pasar el ID como extra
        startActivity(intent)
    }
}

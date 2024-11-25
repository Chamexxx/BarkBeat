package com.reproductor.barkbeat


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.adapters.SongListAdapter
import com.reproductor.barkbeat.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var songsListAdapter: SongListAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el RecyclerView para mostrar los resultados de búsqueda
        setupRecyclerView()

        // Configurar el TextWatcher para realizar la búsqueda en tiempo real
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchSongs(query)
                } else {
                    songsListAdapter.updateSongs(emptyList()) // Limpiar resultados cuando no hay texto
                }
            }
        })

        // Configurar el listener para manejar los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar la barra de navegación inferior
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> true // Ya estamos en SearchActivity
                R.id.nav_library -> {
                    startActivity(Intent(this, PlaylistActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        // Seleccionar el ítem actual para esta actividad
        bottomNavigationView.selectedItemId = R.id.nav_search
    }

    // Configurar el RecyclerView
    private fun setupRecyclerView() {
        songsListAdapter = SongListAdapter(mutableListOf())
        binding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSearchResults.adapter = songsListAdapter
    }

    // Función para realizar la búsqueda de canciones en Firebase
    private fun searchSongs(query: String) {
        Log.d("SearchSongs", "Buscando canciones con el query: $query")

        val normalizedQuery = query.lowercase() // Convertir a minúsculas para una búsqueda uniforme

        // Crear una lista para almacenar los resultados de ambas consultas
        val combinedResults = mutableSetOf<String>()

        // Primera consulta: Buscar por título
        db.collection("songs")
            .get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val title = document.getString("title")?.lowercase() // Convertir el título a minúsculas
                    val subtitle = document.getString("subtitle")?.lowercase() // Convertir el subtítulo a minúsculas

                    // Agregar canciones que coincidan con el título o el subtítulo
                    if (title?.contains(normalizedQuery) == true || subtitle?.contains(normalizedQuery) == true) {
                        combinedResults.add(document.id)
                    }
                }

                // Actualizar el adaptador con los resultados combinados
                if (combinedResults.isNotEmpty()) {
                    songsListAdapter.updateSongs(combinedResults.toList())
                    Log.d("SearchSongs", "Total de resultados combinados: ${combinedResults.size}")
                } else {
                    Log.d("SearchSongs", "No se encontraron resultados")
                    songsListAdapter.updateSongs(emptyList()) // Limpiar lista si no hay resultados
                }
            }
            .addOnFailureListener { e ->
                Log.e("SearchSongs", "Error buscando canciones", e)
            }
    }




}

package com.reproductor.barkbeat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.reproductor.barkbeat.adapters.SongListAdapter
import com.reproductor.barkbeat.databinding.ActivitySongsListBinding
import com.reproductor.barkbeat.models.CategoryModel

class SongsListActivity : AppCompatActivity() {

    companion object {
        lateinit var category: CategoryModel
    }

    private lateinit var binding: ActivitySongsListBinding
    private lateinit var songsListAdapter: SongListAdapter
    private lateinit var db: FirebaseFirestore
    private var lastVisibleDocument: DocumentSnapshot? = null
    private val limit = 5  //Limite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.nameTextView.text = category.name
        Glide.with(binding.coverImageView).load(category.coverUrl)
            .apply(RequestOptions().transform(RoundedCorners(32)))
            .into(binding.coverImageView)

        setupSongsListRecyclerView()
        loadSongs()  // Carga inicial

        // Paginación
        binding.songsListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    loadSongs()  // Carga más canciones cuando se llega al limite establecido
                }
            }
        })
    }

    private fun setupSongsListRecyclerView() {
        songsListAdapter = SongListAdapter(mutableListOf())
        binding.songsListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter = songsListAdapter
    }

    private fun loadSongs() {
        var query = db.collection("songs")
            .whereEqualTo("categoryId", category.name) // Filtrar por categoría
            .limit(limit.toLong()) // Límite de paginación

        // Si ya hemos cargado canciones, continuar desde el último documento visible
        lastVisibleDocument?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                Log.d("Firestore Query", "Canciones cargadas: ${documentSnapshots.size()}")
                if (documentSnapshots.isEmpty) {
                    Log.d("Paginación", "No hay más canciones para cargar.")
                    return@addOnSuccessListener
                }

                // Extraer los datos
                val newSongs = documentSnapshots.documents.mapNotNull { it.id }
                songsListAdapter.addSongs(newSongs) // Añadir al adaptador

                // Actualizar el último documento visible
                lastVisibleDocument = documentSnapshots.documents.last()
                Log.d("Paginación", "Cargados ${newSongs.size} canciones")
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }


}

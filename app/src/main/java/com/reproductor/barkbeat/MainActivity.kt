package com.reproductor.barkbeat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.adapters.CategoryAdapter
import com.reproductor.barkbeat.adapters.SectionSongListAdapter
import com.reproductor.barkbeat.databinding.ActivityMainBinding
import com.reproductor.barkbeat.models.CategoryModel
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)




        // Configurar la navegación de la barra inferior
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Mostrar MainActivity (puedes reemplazar esto con un fragmento si lo prefieres)
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_search -> {
                    // Mostrar SearchActivity (a implementar después)
                    startActivity(Intent(this, SearchActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_library -> {
                    // Mostrar la actividad o fragmento para las playlists
                    startActivity(Intent(this, PlaylistActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        getCategories()
        setupSection("section_Artist", binding.sectionTrendingMainLayout, binding.trendingSection, binding.trendingRecyclerView)

        // Configurar el botón para crear una nueva playlist
        val buttonCrearPlaylist = findViewById<Button>(R.id.buttonCrearPlaylist)
        buttonCrearPlaylist.setOnClickListener {
            mostrarDialogoCrearPlaylist()
        }

        binding.iconUser.setOnClickListener {
            showMenu()
        }
    }

    private fun mostrarDialogoCrearPlaylist() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Crear Playlist")

        val input = EditText(this)
        input.hint = "Nombre de la Playlist"
        builder.setView(input)

        builder.setPositiveButton("Crear") { dialog, _ ->
            val nombrePlaylist = input.text.toString()
            if (nombrePlaylist.isNotEmpty()) {
                crearNuevaPlaylist(nombrePlaylist)
            } else {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun crearNuevaPlaylist(nombrePlaylist: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val nuevaPlaylist = hashMapOf(
                "usuarioId" to userId,
                "nombre" to nombrePlaylist,
                "fechaCreacion" to System.currentTimeMillis()
            )

            db.collection("playlists").add(nuevaPlaylist)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Playlist creada: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al crear la playlist: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun showMenu() {
        val popupMenu = PopupMenu(this, binding.iconUser)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    logout()
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
            false
        }
    }

    fun logout() {
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun getCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>) {
        categoryAdapter = CategoryAdapter(categoryList)
        binding.catRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.catRecyclerView.adapter = categoryAdapter
    }

    fun setupSection(id: String, mainLayout: RelativeLayout, titleView: TextView, recyclerView: RecyclerView) {
        FirebaseFirestore.getInstance().collection("sections")
            .document("trending_sections")
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    binding.trendingSection.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter = SectionSongListAdapter(songs)

                }
            }
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }

    fun showPlayerView() {
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Escuchando: " + it.title + " - " + it.subtitle
            binding.songTitleTextView.isSelected = true
            binding.songTitleTextView.setOnClickListener {
                val intent = Intent(binding.root.context, PlayerActivity::class.java)
                binding.root.context.startActivity(intent)
            }
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility = View.GONE
        }
    }
}
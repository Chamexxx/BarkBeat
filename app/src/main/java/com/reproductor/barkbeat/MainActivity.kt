package com.reproductor.barkbeat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.adapters.CategoryAdapter
import com.reproductor.barkbeat.adapters.SectionSongListAdapter
import com.reproductor.barkbeat.databinding.ActivityMainBinding
import com.reproductor.barkbeat.models.CategoryModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCategories()
        setupSection("section_Artist", binding.sectionTrendingMainLayout,binding.trendingSection,binding.trendingRecyclerView)
    }


    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }


    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.catRecyclerView.layoutManager =LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        binding.catRecyclerView.adapter = categoryAdapter
    }

fun setupSection(id: String, mainLayout:RelativeLayout, titleView : TextView, recyclerView: RecyclerView){

        FirebaseFirestore.getInstance().collection("sections")
            .document("trending_sections")
            .get().addOnSuccessListener {
                val section =it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility=View.VISIBLE //HABILITAR LAS DEMÁS SECCIONES
                    binding.trendingSection.text=name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                    recyclerView.adapter=SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener{
                        SongsListActivity.category =section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }

            }
        }

}

    override fun onResume() {
         super.onResume()
        showPlayerView()
    }

    fun showPlayerView(){
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Escuchando: " + it.title + " - " + it.subtitle
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        }   ?: run{
            binding.playerView.visibility =View.GONE


        }
    }
}

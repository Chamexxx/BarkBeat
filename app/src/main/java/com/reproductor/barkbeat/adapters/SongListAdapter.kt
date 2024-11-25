package com.reproductor.barkbeat.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.MyExoplayer
import com.reproductor.barkbeat.PlayerActivity
import com.reproductor.barkbeat.databinding.SongListItemRecyclerRowBinding
import com.reproductor.barkbeat.models.SongModel

class SongListAdapter(private val songIdList: MutableList<String>) :
    RecyclerView.Adapter<SongListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(songId: String, songNumber: Int) {
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        // Mostrar el número de canción en el TextView
                        binding.songNumberTextView.text = "#$songNumber"  // Aquí # es el número de la canción
                        binding.songTitleTextView.text = title
                        binding.songSubtitleTextView.text = subtitle
                        Glide.with(binding.songCoverImageView).load(coverUrl)
                            .apply(RequestOptions().transform(RoundedCorners(32)))
                            .into(binding.songCoverImageView)
                        binding.root.setOnClickListener {
                            MyExoplayer.startPlaying(binding.root.context, song)
                            it.context.startActivity(Intent(it.context, PlayerActivity::class.java))
                        }
                    }
                }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SongListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return songIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(songIdList[position], position + 1)

    }

    // Método para añadir canciones por paginación

    fun addSongs(newSongs: List<String>) {
        val startPosition = songIdList.size
        songIdList.addAll(newSongs)
        notifyItemRangeInserted(startPosition, newSongs.size)
    }

    fun updateSongs(newSongs: List<String>) {
        songIdList.clear()
        songIdList.addAll(newSongs)
        notifyDataSetChanged()
    }





}

package com.reproductor.barkbeat.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.reproductor.barkbeat.R
import com.reproductor.barkbeat.models.PlaylistModel

class PlaylistAdapter(private val context: Context, private val listener: OnPlaylistClickListener) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var playlists: List<PlaylistModel> = listOf()

    fun setPlaylists(playlists: List<PlaylistModel>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)

        // Configurar el clic normal para navegar a la actividad de canciones
        holder.itemView.setOnClickListener {
            listener.onPlaylistClick(playlist.id)
        }

        // Detectar cuando se mantiene presionado el item para mostrar un diálogo de eliminación
        holder.itemView.setOnLongClickListener {
            showDeleteDialog(playlist.id)
            true
        }
    }

    override fun getItemCount() = playlists.size

    private fun showDeleteDialog(playlistId: String) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Playlist")
            .setMessage("¿Estás seguro de que deseas eliminar esta playlist?")
            .setPositiveButton("Eliminar") { _, _ ->
                deletePlaylist(playlistId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletePlaylist(playlistId: String) {
        val db = FirebaseFirestore.getInstance()

        // Eliminar la playlist de Firestore
        db.collection("playlists").document(playlistId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Playlist eliminada con éxito", Toast.LENGTH_SHORT).show()
                // Eliminar la playlist de la lista localmente
                playlists = playlists.filterNot { it.id == playlistId }
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al eliminar la playlist: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistName: TextView = itemView.findViewById(R.id.playlist_name)

        fun bind(playlist: PlaylistModel) {
            playlistName.text = playlist.nombre
        }
    }

    interface OnPlaylistClickListener {
        fun onPlaylistClick(playlistId: String)
    }
}

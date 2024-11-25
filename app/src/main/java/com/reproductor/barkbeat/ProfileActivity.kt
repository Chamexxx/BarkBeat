package com.reproductor.barkbeat

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        MostrarDatos()
    }
    private fun MostrarDatos() {
        val userId = FirebaseAuth.getInstance().currentUser?.email

        val email = findViewById<TextView>(R.id.id_profileText)
        email.text = userId

    }

    private fun CambioContraseña(){
        val nueva_contraseña = "nueva_contraseña"
        val userId = FirebaseAuth.getInstance().currentUser?.updatePassword(nueva_contraseña)

    }
}
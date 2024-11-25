package com.reproductor.barkbeat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer el layout para el Splash Screen
        setContentView(R.layout.activity_splash)

        // Añadir un retraso para simular el splash screen
        Handler().postDelayed({
            // Verifica si el usuario está autenticado
            val user = FirebaseAuth.getInstance().currentUser

            // Si el usuario está autenticado, lo llevamos a MainActivity
            if (user != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Si no está autenticado, lo llevamos a LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // Cierra la actividad SplashScreen para que no pueda regresar
            finish()
        }, 2000) // 2000 ms = 2 segundos de retraso
    }
}
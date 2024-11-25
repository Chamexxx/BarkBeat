package com.reproductor.barkbeat

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.reproductor.barkbeat.databinding.ActivityLoginBinding
import com.reproductor.barkbeat.databinding.ActivitySignUpBinding
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    // Variable para saber si la contraseña está visible o no
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuramos el click listener para el botón de inicio de sesión
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()

            if (!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)) {
                binding.emailEdittext.error = "Dirección de correo electrónico inválida"
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.passwordEdittext.error = "La contraseña debe tener por lo menos 8 caracteres"
                return@setOnClickListener
            }

            loginFirebase(email, password)
        }

        // Configuramos el click listener para el botón de registro
        binding.gotoSignupBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Configuramos el click listener para el botón de mostrar/ocultar contraseña
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    // Función para alternar la visibilidad de la contraseña
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Si la contraseña está visible, la ocultamos
            binding.passwordEdittext.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.icon_eye)  // Ícono de ojo cerrado
        } else {
            // Si la contraseña está oculta, la mostramos
            binding.passwordEdittext.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.icon_eye)  // Ícono de ojo abierto
        }

        // Cambiamos el estado de visibilidad
        isPasswordVisible = !isPasswordVisible

        // Actualizamos el cursor en el campo de contraseña
        binding.passwordEdittext.setSelection(binding.passwordEdittext.text.length)
    }

    // Función para el login con Firebase
    fun loginFirebase(email: String, password: String) {
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                setInProgress(false)
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Ups, hubo un problema al iniciar sesión", Toast.LENGTH_SHORT).show()
                setInProgress(false)
            }
    }

    // Función para cambiar el estado del botón de login y mostrar el progreso
    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.loginBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.loginBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}

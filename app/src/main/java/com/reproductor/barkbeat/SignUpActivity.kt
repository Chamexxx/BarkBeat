package com.reproductor.barkbeat

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.reproductor.barkbeat.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toggle visibility for password
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Toggle visibility for confirm password
        binding.btnToggleConfirmPassword.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        binding.createAccountBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()

            // Validación de longitud de la contraseña
            if (password.length < 8) {
                binding.passwordEdittext.error = "La contraseña debe tener al menos 8 caracteres"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.confirmPasswordEdittext.error = "Las contraseñas no coinciden"
                return@setOnClickListener
            }

            createAccountFirebase(email, password)
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.icon_eye)
        } else {
            binding.passwordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.icon_eye)
        }
        binding.passwordEdittext.setSelection(binding.passwordEdittext.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            binding.confirmPasswordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.icon_eye)
        } else {
            binding.confirmPasswordEdittext.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.icon_eye)
        }
        binding.confirmPasswordEdittext.setSelection(binding.confirmPasswordEdittext.text.length)
        isConfirmPasswordVisible = !isConfirmPasswordVisible
    }

    fun createAccountFirebase(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Cuenta creada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

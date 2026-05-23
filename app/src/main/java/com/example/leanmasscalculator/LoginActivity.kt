package com.example.leanmasscalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val success = databaseHelper.loginUser(email, password)

        if (success) {
            val sharedPreferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("email", email)
                .putBoolean("isLoggedIn", true)
                .apply()

            Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
        }
    }
}
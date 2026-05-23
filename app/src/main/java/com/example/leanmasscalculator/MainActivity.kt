package com.example.leanmasscalculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        val sharedPreferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", "") ?: ""

        if (userEmail.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.btnCalculate.setOnClickListener {
            calculateLeanMass()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun calculateLeanMass() {
        val weightText = binding.etWeight.text.toString().trim()
        val heightText = binding.etHeight.text.toString().trim()

        if (weightText.isEmpty() || heightText.isEmpty()) {
            Toast.makeText(this, "Veuillez saisir le poids et la taille", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightText.toDoubleOrNull()
        val height = heightText.toDoubleOrNull()

        if (weight == null || height == null || weight <= 0 || height <= 0) {
            Toast.makeText(this, "Valeurs invalides", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = if (binding.rbMale.isChecked) "Homme" else "Femme"

        val lbm = Config.calculateLBM(gender, weight, height)
        val satisfactory = Config.isSatisfactory(gender, lbm)

        val status = if (satisfactory) {
            "Résultat satisfaisant"
        } else {
            "Résultat à surveiller"
        }

        binding.tvResult.text = "LBM = %.2f kg".format(lbm)
        binding.tvStatus.text = status
        binding.tvStatus.setTextColor(Color.BLACK)

        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val saved = databaseHelper.addHistory(
            userEmail = userEmail,
            gender = gender,
            weight = weight,
            height = height,
            lbm = lbm,
            status = status,
            date = date
        )

        if (saved) {
            Toast.makeText(this, "Calcul enregistré dans l'historique", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.leanmasscalculator

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        val sharedPreferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", "") ?: ""

        loadHistory()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnClearHistory.setOnClickListener {
            val deleted = databaseHelper.clearHistory(userEmail)
            if (deleted) {
                Toast.makeText(this, "Historique supprimé", Toast.LENGTH_SHORT).show()
                loadHistory()
            } else {
                Toast.makeText(this, "Aucun historique à supprimer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHistory() {
        binding.historyContainer.removeAllViews()

        val historyList = databaseHelper.getHistory(userEmail)

        if (historyList.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "Aucun calcul enregistré"
            emptyText.textSize = 18f
            emptyText.setTextColor(Color.DKGRAY)
            emptyText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            emptyText.setPadding(0, 40, 0, 0)
            binding.historyContainer.addView(emptyText)
            return
        }

        for (item in historyList) {
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_history, binding.historyContainer, false)

            val tvHistoryResult = itemView.findViewById<TextView>(R.id.tvHistoryResult)
            val tvHistoryDetails = itemView.findViewById<TextView>(R.id.tvHistoryDetails)
            val tvHistoryStatus = itemView.findViewById<TextView>(R.id.tvHistoryStatus)
            val tvHistoryDate = itemView.findViewById<TextView>(R.id.tvHistoryDate)
            val btnDeleteItem = itemView.findViewById<Button>(R.id.btnDeleteItem)

            tvHistoryResult.text = "LBM = %.2f kg".format(item.lbm)
            tvHistoryDetails.text =
                "${item.gender} | Poids: ${item.weight} kg | Taille: ${item.height} cm"
            tvHistoryStatus.text = item.status
                .replace("\u2705 ", "")
                .replace("\u26A0\uFE0F ", "")
                .replace("\u26A0 ", "")
            tvHistoryDate.text = item.date
            tvHistoryStatus.setTextColor(Color.BLACK)

            btnDeleteItem.setOnClickListener {
                val deleted = databaseHelper.deleteHistory(item.id)
                if (deleted) {
                    Toast.makeText(this, "Calcul supprimé", Toast.LENGTH_SHORT).show()
                    loadHistory()
                } else {
                    Toast.makeText(this, "Erreur de suppression", Toast.LENGTH_SHORT).show()
                }
            }

            binding.historyContainer.addView(itemView)
        }
    }
}

package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationmyday.data.DiaryEntry
import com.example.myapplicationmyday.databinding.ActivityHomeBinding
import com.example.myapplicationmyday.viewmodel.DiaryViewModel

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: DiaryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupObservers()
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        // Refrescar estadísticas cuando volvemos a esta pantalla
        viewModel.allEntries.value?.let { entries ->
            updateStatistics(entries)
        }
    }
    
    private fun setupObservers() {
        viewModel.allEntries.observe(this) { entries ->
            updateStatistics(entries)
        }
    }
    
    private fun updateStatistics(entries: List<DiaryEntry>) {
        // Actualizar estadísticas
        val entryCount = entries.size
        binding.tvEntryCount.text = entryCount.toString()
        
        // Calcular días de racha (días consecutivos con entradas)
        val streakDays = calculateStreakDays(entries.map { it.date })
        binding.tvStreakDays.text = streakDays.toString()
        
        // Contar palabras totales
        val totalWords = entries.sumOf { entry ->
            (entry.title.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size + 
             entry.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size)
        }
        binding.tvTotalWords.text = totalWords.toString()
        
        // Actualizar contadores de diarios
        binding.tvAllEntriesCount.text = entryCount.toString()
        binding.tvDiaryCount.text = entryCount.toString()
        
        // Actualizar texto descriptivo
        binding.tvEntryLabel.text = if (entryCount == 1) "entrada este año" else "entradas este año"
    }
    
    private fun calculateStreakDays(dates: List<Long>): Int {
        if (dates.isEmpty()) return 0
        
        val sortedDates = dates.map { it / (24 * 60 * 60 * 1000) }.distinct().sortedDescending()
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        
        if (!sortedDates.contains(today)) return 0
        
        var streak = 1
        for (i in 0 until sortedDates.size - 1) {
            if (sortedDates[i] - sortedDates[i + 1] == 1L) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
    
    private fun setupClickListeners() {
        binding.btnMore.setOnClickListener {
            // TODO: Mostrar menú de opciones
        }
        
        // Click en tarjeta de estadísticas
        binding.cardStatistics.setOnClickListener {
            // TODO: Abrir pantalla de estadísticas detalladas
        }
        
        // Click en tarjeta de lugares
        binding.cardPlaces.setOnClickListener {
            // TODO: Abrir pantalla de lugares
        }
        
        // Click en "Todas las entradas"
        binding.layoutAllEntries.setOnClickListener {
            openDiaryScreen()
        }
        
        // Click en "Diario"
        binding.layoutDiary.setOnClickListener {
            openDiaryScreen()
        }
        
        // Click en "Social Media"
        binding.layoutSocialMedia.setOnClickListener {
            // TODO: Abrir pantalla de Social Media
        }
        
        // Click en "Eliminadas recientemente"
        binding.layoutDeleted.setOnClickListener {
            val intent = Intent(this, DeletedEntriesActivity::class.java)
            startActivity(intent)
        }
        
        // Botón para crear nuevo diario
        binding.btnAddDiary.setOnClickListener {
            // TODO: Mostrar diálogo para crear nuevo diario
        }
    }
    
    private fun openDiaryScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

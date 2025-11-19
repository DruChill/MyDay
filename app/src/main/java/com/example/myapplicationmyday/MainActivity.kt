package com.example.myapplicationmyday

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationmyday.adapter.DiaryAdapter
import com.example.myapplicationmyday.databinding.ActivityMainBinding
import com.example.myapplicationmyday.viewmodel.DiaryViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var adapter: DiaryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = DiaryAdapter { entry ->
            // Abrir la pantalla de edición/vista de entrada
            val intent = Intent(this, AddEditEntryActivity::class.java).apply {
                putExtra("ENTRY_ID", entry.id)
            }
            startActivity(intent)
        }
        
        binding.rvDiaryEntries.layoutManager = LinearLayoutManager(this)
        binding.rvDiaryEntries.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.allEntries.observe(this) { entries ->
            adapter.submitList(entries)
            
            // Mostrar/ocultar el estado vacío
            if (entries.isEmpty()) {
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.rvDiaryEntries.visibility = View.GONE
            } else {
                binding.emptyStateLayout.visibility = View.GONE
                binding.rvDiaryEntries.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.fabAddEntry.setOnClickListener {
            val intent = Intent(this, AddEditEntryActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnBack.setOnClickListener {
            // Acción para el botón de atrás (opcional)
            finish()
        }
        
        binding.btnSearch.setOnClickListener {
            // TODO: Implementar búsqueda
        }
        
        binding.btnMore.setOnClickListener {
            // TODO: Mostrar menú de opciones
        }
    }
}
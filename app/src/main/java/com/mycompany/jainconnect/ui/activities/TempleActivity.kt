package com.mycompany.jainconnect.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.TempleAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel

@AndroidEntryPoint
class TempleActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: TempleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_temple)

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerViewTemples)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TempleAdapter()
        recyclerView.adapter = adapter

        val etSearch = findViewById<android.widget.EditText>(R.id.etSearchTemple)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterTemples(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        viewModel.templeList.observe(this) { list ->
            progressBar.visibility = View.GONE
            adapter.submitList(list)
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddTemple).setOnClickListener {
            val intent = android.content.Intent(this, AddTempleActivity::class.java)
            startActivity(intent)
        }

        progressBar.visibility = View.VISIBLE
        viewModel.fetchTemples()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTemples()
    }
}

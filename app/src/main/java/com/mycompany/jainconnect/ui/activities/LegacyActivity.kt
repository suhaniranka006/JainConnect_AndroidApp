package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.StoryAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LegacyActivity : AppCompatActivity() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        viewModel.fetchStories(this)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        val rvStories = findViewById<RecyclerView>(R.id.rvStories)
        rvStories.layoutManager = LinearLayoutManager(this)

        adapter = StoryAdapter(
            emptyList(),
            onLikeClick = { storyId ->
                viewModel.likeStory(this, storyId)
            },
            onShareClick = { story ->
                shareStory(story.title, story.summary, story.imageUrl)
            },
            onItemClick = { story ->
                val intent = Intent(this, StoryDetailActivity::class.java)
                intent.putExtra("story_json", com.google.gson.Gson().toJson(story))
                startActivity(intent)
            }
        )
        
        // Handle Save Click
        adapter.setOnSaveClickListener { story ->
             viewModel.toggleSaveState(story.id, "saved_stories") // Matching SavedRepository key
        }
        
        rvStories.adapter = adapter

        // --- CACHE LOAD STORIES ---
        val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val cachedData = sharedPref.getString("cached_stories", null)
        if (cachedData != null) {
            val type = object : TypeToken<List<com.mycompany.jainconnect.data.models.Story>>() {}.type
            val list: List<com.mycompany.jainconnect.data.models.Story> = gson.fromJson(cachedData, type)
            if (list.isNotEmpty()) {
                adapter.updateList(list)
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            }
        }
        
        // --- LOAD SAVED STATE ---
        // Manually reading for now as simpler than exposing Repo through VM
        val savedPref = getSharedPreferences("jain_saved_items", Context.MODE_PRIVATE)
        val savedJson = savedPref.getString("saved_stories", null) // KEY_STORIES
        if (savedJson != null) {
             val setType = object : TypeToken<Set<String>>() {}.type
             val savedIds: Set<String> = gson.fromJson(savedJson, setType)
             adapter.updateSavedIds(savedIds)
        }
        // ------------------
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        viewModel.storyList.observe(this) { stories ->
            progressBar.visibility = View.GONE // Hide immediately on any result
            adapter.updateList(stories)
            
            if (stories.isNotEmpty()) {
                // --- CACHE SAVE ---
                val sharedPref = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                val json = gson.toJson(stories)
                sharedPref.edit().putString("cached_stories", json).apply()
                // ------------------
            } else {
                 // Optional: Show "No Stories" text view if needed
            }
        }
    }

    private fun shareStory(title: String, summary: String, imageUrl: String) {
        val shareText = "🌟 *Jain Legacy: $title*\n\n$summary\n\nRead more on JainConnect App!\n$imageUrl"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Story via"))
    }
}

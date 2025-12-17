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
        rvStories.adapter = adapter
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        viewModel.storyList.observe(this) { stories ->
            adapter.updateList(stories)
            progressBar.visibility = if (stories.isEmpty()) View.VISIBLE else View.GONE
            // If empty list is returned after loading, hide progress bar anyway (simple logic for now)
            if (stories.isEmpty()) progressBar.visibility = View.GONE
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

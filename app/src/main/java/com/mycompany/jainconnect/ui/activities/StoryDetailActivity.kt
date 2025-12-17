package com.mycompany.jainconnect.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mycompany.jainconnect.R
import com.google.gson.Gson
import com.mycompany.jainconnect.data.models.Story

class StoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        val storyJson = intent.getStringExtra("story_json")
        val story = Gson().fromJson(storyJson, Story::class.java)

        setupToolbar(story.title)
        setupViews(story)
    }

    private fun setupToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar)
        collapsingToolbar.title = title
    }

    private fun setupViews(story: Story) {
        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvDetailTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDetailDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDetailSource = findViewById<TextView>(R.id.tvDetailSource)
        val tvDetailContent = findViewById<TextView>(R.id.tvDetailContent)
        val fabShare = findViewById<FloatingActionButton>(R.id.fabShare)

        tvDetailTitle.text = story.title
        // Use full content here
        tvDetailContent.text = story.content
        tvDetailSource.text = "Source: ${story.source ?: "Unknown"}"
        
        // Simple Date formatting if needed, or raw string
        tvDetailDate.text = story.createdAt.take(10) // First 10 chars (YYYY-MM-DD)

        Glide.with(this)
            .load(story.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(ivDetailImage)

        fabShare.setOnClickListener {
            val shareText = "🌟 *${story.title}*\n\n${story.summary}\n\nRead full story on JainConnect App!\n${story.imageUrl}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, story.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Story via"))
        }
    }
}

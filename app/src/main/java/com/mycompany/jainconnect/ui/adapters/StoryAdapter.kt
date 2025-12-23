package com.mycompany.jainconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.Story

class StoryAdapter(
    private var stories: List<Story>,
    private val onLikeClick: (String) -> Unit,
    private val onShareClick: (Story) -> Unit,
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private var savedIds: Set<String> = emptySet()
    
    fun setOnSaveClickListener(listener: (Story) -> Unit) {
        onSaveClick = listener
    }
    private var onSaveClick: ((Story) -> Unit)? = null

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivStoryImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvStoryTitle)
        val tvSource: TextView = itemView.findViewById(R.id.tvStorySource)
        val tvSummary: TextView = itemView.findViewById(R.id.tvStorySummary)
        val btnLike: LinearLayout = itemView.findViewById(R.id.btnLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val btnShare: ImageView = itemView.findViewById(R.id.btnShare)
        val btnSave: ImageView = itemView.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]

        holder.tvTitle.text = story.title
        holder.tvSummary.text = story.summary
        holder.tvLikeCount.text = "${story.likes} Likes"

        if (!story.source.isNullOrEmpty()) {
            holder.tvSource.visibility = View.VISIBLE
            holder.tvSource.text = "Source: ${story.source}"
        } else {
            holder.tvSource.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(story.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.ivImage)

        // Open Detail View on Item Click
        holder.itemView.setOnClickListener {
            onItemClick(story)
        }

        // Initial State
        updateLikeIcon(holder, story.isLiked)
        updateSaveIcon(holder, savedIds.contains(story.id))

        // Handle Like Click
        holder.btnLike.setOnClickListener {
            // 1. Toggle Local State
            story.isLiked = !story.isLiked
            
            // 2. Update UI Immediately (Optimistic)
            updateLikeIcon(holder, story.isLiked)
            
            // 3. Update Count temporarily (optional, but good for UX)
            if (story.isLiked) {
               holder.tvLikeCount.text = "${story.likes + 1} Likes"
            } else {
               holder.tvLikeCount.text = "${story.likes} Likes"
            }

            // 4. Notify Parent/ViewModel
            onLikeClick(story.id)
        }

        // Handle Share Click
        holder.btnShare.setOnClickListener {
            onShareClick(story)
        }
        
        // Handle Save Click
        holder.btnSave.setOnClickListener {
            onSaveClick?.invoke(story)
            // Optimistic Update
            val isCurrentlySaved = savedIds.contains(story.id)
            if (isCurrentlySaved) {
                savedIds = savedIds - story.id
            } else {
                savedIds = savedIds + story.id
            }
            updateSaveIcon(holder, !isCurrentlySaved)
        }
    }

    override fun getItemCount() = stories.size

    fun updateList(newList: List<Story>) {
        stories = newList
        notifyDataSetChanged()
    }
    
    fun updateSavedIds(newIds: Set<String>) {
        savedIds = newIds
        notifyDataSetChanged()
    }

    private fun updateLikeIcon(holder: StoryViewHolder, isLiked: Boolean) {
        val ivHeart: ImageView = holder.itemView.findViewById(R.id.ivHeart)
        if (isLiked) {
            ivHeart.setImageResource(R.drawable.ic_favorite_filled)
            ivHeart.setColorFilter(null) // Reset tint if drawable has its own color or apply red
        } else {
            ivHeart.setImageResource(R.drawable.ic_favorite_border)
        }
    }
    
    private fun updateSaveIcon(holder: StoryViewHolder, isSaved: Boolean) {
        if (isSaved) {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_filled)
            holder.btnSave.setColorFilter(null)
        } else {
            holder.btnSave.setImageResource(R.drawable.ic_bookmark_border)
        }
    }
}

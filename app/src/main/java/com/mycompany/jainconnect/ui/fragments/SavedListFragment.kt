package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.EventAdapter

import com.mycompany.jainconnect.ui.adapters.MaharajAdapter
import com.mycompany.jainconnect.ui.adapters.TithiAdapter
import com.mycompany.jainconnect.ui.adapters.TempleAdapter
import com.mycompany.jainconnect.ui.adapters.BhojanshalaAdapter
import com.mycompany.jainconnect.ui.adapters.OnRsvpButtonClickListener
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.mycompany.jainconnect.data.models.Event
import com.mycompany.jainconnect.data.repository.SavedRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedListFragment : Fragment(), OnRsvpButtonClickListener {

    private var category: String? = null
    private val viewModel: JainViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_CATEGORY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = android.widget.FrameLayout(requireContext())
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)
        view.addView(recyclerView)

        emptyView = TextView(context)
        emptyView.text = "No Saved $category yet"
        emptyView.textSize = 18f
        emptyView.setTextColor(android.graphics.Color.GRAY)
        emptyView.gravity = android.view.Gravity.CENTER
        view.addView(emptyView)
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // This fragment might be instantiated multiple times inside ViewPager
        // We need to fetch data based on category
        setupData()
    }
    
    private fun setupData() {
        when (category) {
            "Monks" -> {
                viewModel.fetchSavedMonks()
                val adapter = MaharajAdapter(emptyList())
                recyclerView.adapter = adapter
                
                adapter.setOnSaveClickListener { maharaj ->
                    viewModel.toggleSaveState(maharaj.id ?: "", SavedRepository.KEY_MONKS)
                }
                
                viewModel.savedMonks.observe(viewLifecycleOwner, Observer { list ->
                    if (list.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.updateData(list)
                        // Also update ids so icons are correct (should all be saved)
                        val ids = list.mapNotNull { it.id }.toSet()
                        adapter.updateSavedIds(ids)
                    }
                })
            }
            "Events" -> {
                viewModel.fetchSavedEvents()
                // EventAdapter needs a listener for RSVP too. passing 'this'
                val adapter = EventAdapter(emptyList(), this)
                recyclerView.adapter = adapter
                
                adapter.setOnSaveClickListener { event ->
                    // Correct mapping check: Event model has _id as per my recent read
                    viewModel.toggleSaveState(event._id, SavedRepository.KEY_EVENTS)
                }

                viewModel.savedEvents.observe(viewLifecycleOwner, Observer { list ->
                    if (list.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.updateData(list)
                        val ids = list.map { it._id }.toSet()
                        adapter.updateSavedIds(ids)
                    }
                })
            }
            "Tithis" -> {

                 viewModel.fetchSavedTithis()
                 val adapter = TithiAdapter(emptyList())
                 recyclerView.adapter = adapter
                 
                 adapter.setOnSaveClickListener { tithi ->
                     viewModel.toggleSaveState(tithi.id ?: "", SavedRepository.KEY_TITHIS)
                 }
                 
                 viewModel.savedTithis.observe(viewLifecycleOwner, Observer { list ->
                     if (list.isNullOrEmpty()) {
                         recyclerView.visibility = View.GONE
                         emptyView.visibility = View.VISIBLE
                     } else {
                         recyclerView.visibility = View.VISIBLE
                         emptyView.visibility = View.GONE
                         adapter.updateData(list)
                         val ids = list.mapNotNull { it.id }.toSet()
                         adapter.updateSavedIds(ids)
                     }
                 })
            }
            "Temples" -> {
                 viewModel.fetchSavedTemples()
                 val adapter = TempleAdapter { temple ->
                     // Handle click -> Open Details? TempleDetailActivity
                     val intent = android.content.Intent(requireContext(), com.mycompany.jainconnect.ui.activities.TempleDetailActivity::class.java)
                     intent.putExtra("temple_data", temple)
                     startActivity(intent)
                 }
                 recyclerView.adapter = adapter
                 
                 adapter.setOnSaveClickListener { temple ->
                     viewModel.toggleSaveState(temple._id, SavedRepository.KEY_TEMPLES)
                 }
                 
                 viewModel.savedTemples.observe(viewLifecycleOwner, Observer { list ->
                     if (list.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                     } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.submitList(list)
                        val ids = list.map { it._id }.toSet()
                        adapter.updateSavedIds(ids)
                     }
                 })
            }
            "Food Corners" -> {
                 viewModel.fetchSavedBhojanshalas()
                 val adapter = BhojanshalaAdapter()
                 recyclerView.adapter = adapter
                 
                 adapter.setOnSaveClickListener { bhojanshala ->
                     viewModel.toggleSaveState(bhojanshala._id, SavedRepository.KEY_FOOD)
                 }
                 
                 viewModel.savedBhojanshalas.observe(viewLifecycleOwner, Observer { list ->
                     if (list.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                     } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.submitList(list)
                        val ids = list.map { it._id }.toSet()
                        adapter.updateSavedIds(ids)
                     }
                 })
            }
            "Stories" -> { // New Case for Stories
                 viewModel.fetchSavedStories()
                 val adapter = com.mycompany.jainconnect.ui.adapters.StoryAdapter(
                     emptyList(),
                     onLikeClick = { /* No-op or handle locally */ },
                     onShareClick = { /* No-op or duplicate share logic */ },
                     onItemClick = { story ->
                         val intent = android.content.Intent(requireContext(), com.mycompany.jainconnect.ui.activities.StoryDetailActivity::class.java)
                         intent.putExtra("story_json", com.google.gson.Gson().toJson(story))
                         startActivity(intent)
                     }
                 )
                 recyclerView.adapter = adapter
                 
                 adapter.setOnSaveClickListener { story ->
                     viewModel.toggleSaveState(story.id, SavedRepository.KEY_STORIES)
                 }
                 
                 viewModel.savedStories.observe(viewLifecycleOwner, Observer { list ->
                     if (list.isNullOrEmpty()) {
                        recyclerView.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                     } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        adapter.updateList(list)
                        val ids = list.map { it.id }.toSet()
                        adapter.updateSavedIds(ids)
                     }
                 })
            }

            else -> {
                emptyView.text = "Saved $category coming soon"
            }
        }
    }
    
    // Stub for RSVP interface
    override fun onRsvpClick(event: Event) {
       // Ideally duplicate logic from EventActivity or share it
       // For now just show toast or ignore in saved screen
       // Or simpler: viewModel.toggleEventRsvp(...)
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: String) =
            SavedListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
    }
}

package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.LeaderboardUser
import com.mycompany.jainconnect.ui.adapters.LeaderboardAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardFragment : Fragment() {

    private val viewModel: JainViewModel by viewModels()
    private lateinit var adapter: LeaderboardAdapter
    
    // UI Elements
    private lateinit var podiumLayout: View
    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Podium Views
    private lateinit var ivFirst: ImageView
    private lateinit var tvFirstName: TextView
    private lateinit var tvFirstScore: TextView
    
    private lateinit var ivSecond: ImageView
    private lateinit var tvSecondName: TextView
    private lateinit var tvSecondScore: TextView
    
    private lateinit var ivThird: ImageView
    private lateinit var tvThirdName: TextView
    private lateinit var tvThirdScore: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Views
        podiumLayout = view.findViewById(R.id.podiumLayout)
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard)
        progressBar = view.findViewById(R.id.progressBar)
        
        ivFirst = view.findViewById(R.id.ivFirstPlace)
        tvFirstName = view.findViewById(R.id.tvFirstName)
        tvFirstScore = view.findViewById(R.id.tvFirstScore)
        
        ivSecond = view.findViewById(R.id.ivSecondPlace)
        tvSecondName = view.findViewById(R.id.tvSecondName)
        tvSecondScore = view.findViewById(R.id.tvSecondScore)
        
        ivThird = view.findViewById(R.id.ivThirdPlace)
        tvThirdName = view.findViewById(R.id.tvThirdName)
        tvThirdScore = view.findViewById(R.id.tvThirdScore)

        adapter = LeaderboardAdapter(emptyList())
        rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.adapter = adapter

        // Observers
        viewModel.leaderboardData.observe(viewLifecycleOwner) { users ->
            progressBar.visibility = View.GONE
            setupLeaderboard(users)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { err ->
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
        }

        // Fetch Data
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        val sharedPref = requireActivity().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        if (token != null) {
            progressBar.visibility = View.VISIBLE
            viewModel.getLeaderboard(token, "alltime") 
        } else {
             Toast.makeText(requireContext(), "Please login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLeaderboard(users: List<LeaderboardUser>) {
        if (users.isEmpty()) {
            podiumLayout.visibility = View.GONE
            return
        }
        podiumLayout.visibility = View.VISIBLE
        
        // Setup Top 3
        if (users.isNotEmpty()) bindUserToPodium(users[0], ivFirst, tvFirstName, tvFirstScore) // 1st
        if (users.size > 1) bindUserToPodium(users[1], ivSecond, tvSecondName, tvSecondScore) // 2nd
        if (users.size > 2) bindUserToPodium(users[2], ivThird, tvThirdName, tvThirdScore) // 3rd
        
        // Setup Rest list (4 onwards)
        if (users.size > 3) {
            val rest = users.subList(3, users.size)
            adapter.list = rest
            adapter.notifyDataSetChanged()
        } else {
            adapter.list = emptyList()
            adapter.notifyDataSetChanged()
        }
    }

    private fun bindUserToPodium(user: LeaderboardUser, iv: ImageView, tvName: TextView, tvScore: TextView) {
        tvName.text = user.name
        tvScore.text = user.coins.toString()
        
        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(this).load(user.profileImage).placeholder(R.drawable.ic_profile_placeholder).into(iv)
        } else {
            iv.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }
}

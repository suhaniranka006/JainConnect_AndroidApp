package com.mycompany.jainconnect.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.MyYatraAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TirthyatraCommunityFragment : Fragment() {

    private val viewModel: JainViewModel by activityViewModels()
    private lateinit var adapter: MyYatraAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Updated layout reference
        return inflater.inflate(R.layout.fragment_tirthyatra_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBarCommunity)
        tvEmptyState = view.findViewById(R.id.tvEmptyCommunity)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPublicYatras)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Using same adapter for now. In future, maybe different actions.
        // Using same adapter with default params (user list)
        adapter = MyYatraAdapter(
            yatras = emptyList(),
            onItemClick = { yatra ->
                val intent = android.content.Intent(context, com.mycompany.jainconnect.ui.activities.TirthyatraDetailsActivity::class.java)
                intent.putExtra("YATRA_DATA", yatra)
                startActivity(intent)
            },
            showDeleteButton = false // Hide delete button for public list
        )
        recyclerView.adapter = adapter

        viewModel.publicYatras.observe(viewLifecycleOwner) { yatras ->
            progressBar.visibility = View.GONE
            if (yatras.isNullOrEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            } else {
                tvEmptyState.visibility = View.GONE
                adapter.updateData(yatras)
            }
        }

        fetchData()
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    private fun fetchData() {
        val sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", "") ?: ""

        if (token.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            viewModel.fetchPublicYatras(token)
        } else {
            tvEmptyState.text = "Please Login to see Community Yatras"
            tvEmptyState.visibility = View.VISIBLE
        }
    }
}

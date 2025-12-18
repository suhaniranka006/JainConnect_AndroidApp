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
class TirthyatraMyYatrasFragment : Fragment() {

    private val viewModel: JainViewModel by activityViewModels()
    private lateinit var adapter: MyYatraAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tirthyatra_my_yatras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBarYatras)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMyYatras)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = MyYatraAdapter(emptyList(),
            onItemClick = { yatra ->
                val intent = android.content.Intent(context, com.mycompany.jainconnect.ui.activities.TirthyatraDetailsActivity::class.java)
                intent.putExtra("YATRA_DATA", yatra)
                startActivity(intent)
            },
            onDeleteClick = { yatra ->
                val sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("jwt_token", "") ?: ""
                if (token.isNotEmpty() && yatra.id != null) {
                    // Confirm Dialog
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete Yatra")
                        .setMessage("Are you sure you want to delete this trip?")
                        .setPositiveButton("Yes") { _, _ ->
                            viewModel.deleteYatra(token, yatra.id)
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
        )
        recyclerView.adapter = adapter

        // Observer
        viewModel.myYatras.observe(viewLifecycleOwner) { yatras ->
            progressBar.visibility = View.GONE
            if (yatras.isNullOrEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                adapter.updateData(emptyList())
            } else {
                tvEmptyState.visibility = View.GONE
                adapter.updateData(yatras)
            }
        }

        viewModel.yatraOperationResult.observe(viewLifecycleOwner) { result ->
            if (result == "Deleted") {
                 Toast.makeText(context, "Yatra deleted successfully", Toast.LENGTH_SHORT).show()
            } else if (result.startsWith("Failed")) {
                 Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch
        fetchData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when coming back (in case created new one)
        fetchData()
    }

    private fun fetchData() {
        val sharedPreferences = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", "") ?: ""

        if (token.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            // Clear current data to prevent showing stale items while loading
            adapter.updateData(emptyList())
            viewModel.fetchMyYatras(token)
        } else {
            tvEmptyState.text = "Please Login to see your Yatras"
            tvEmptyState.visibility = View.VISIBLE
        }
    }
}

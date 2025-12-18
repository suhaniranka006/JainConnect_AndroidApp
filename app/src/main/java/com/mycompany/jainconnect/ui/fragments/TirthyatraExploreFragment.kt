package com.mycompany.jainconnect.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.TirthyatraTemplateAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.mycompany.jainconnect.ui.activities.CreateYatraActivity // Will create this next
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TirthyatraExploreFragment : Fragment() {

    private val viewModel: JainViewModel by activityViewModels() // Shared ViewModel with Activity if needed, or just viewModels()
    private lateinit var adapter: TirthyatraTemplateAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tirthyatra_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBarExplore)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTirthyatraTemplates)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = TirthyatraTemplateAdapter(emptyList()) { template ->
            // Handle "Plan Trip" click
            val intent = Intent(requireContext(), CreateYatraActivity::class.java)
            intent.putExtra("TEMPLATE_ID", template.id)
            intent.putExtra("TEMPLATE_TITLE", template.title)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Observe Data
        viewModel.tirthyatraTemplates.observe(viewLifecycleOwner) { templates ->
            adapter.updateData(templates)
            progressBar.visibility = View.GONE
        }

        // Fetch Data
        progressBar.visibility = View.VISIBLE
        viewModel.fetchTirthyatraTemplates()
    }
}

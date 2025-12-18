package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.mycompany.jainconnect.data.models.Tirthyatra

class YatraChecklistFragment : Fragment() {
    private var tirthyatra: Tirthyatra? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tirthyatra = it.getParcelable("YATRA_DATA")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val listView = ListView(requireContext())
        tirthyatra?.let { yatra ->
            val items = yatra.checklist.map { it.item }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_checked, items)
            listView.adapter = adapter
            listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            
            // Set checked based on data
            yatra.checklist.forEachIndexed { index, checklistItem ->
                listView.setItemChecked(index, checklistItem.isChecked)
            }
        }
        return listView
    }

    companion object {
        fun newInstance(yatra: Tirthyatra) = YatraChecklistFragment().apply {
            arguments = Bundle().apply { putParcelable("YATRA_DATA", yatra) }
        }
    }
}

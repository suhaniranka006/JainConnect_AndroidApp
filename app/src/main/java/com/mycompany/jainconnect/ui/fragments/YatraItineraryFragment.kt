package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.ui.adapters.ItineraryAdapter

class YatraItineraryFragment : Fragment() {

    private var tirthyatra: Tirthyatra? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tirthyatra = it.getParcelable("YATRA_DATA")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = RecyclerView(requireContext())
        view.layoutManager = LinearLayoutManager(context)
        
        tirthyatra?.let { 
            view.adapter = ItineraryAdapter(it.itinerary)
        }
        
        return view
    }

    companion object {
        fun newInstance(yatra: Tirthyatra) =
            YatraItineraryFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("YATRA_DATA", yatra)
                }
            }
    }
}

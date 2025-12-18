package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.ui.adapters.MembersAdapter
import com.mycompany.jainconnect.data.models.Tirthyatra

class YatraMembersFragment : Fragment() {
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
        
        // Initial load
        updateList(view)
        
        return view
    }

    fun updateData(newYatra: Tirthyatra) {
        this.tirthyatra = newYatra
        val view = view // Get current view (RecyclerView)
        if (view is RecyclerView) {
            updateList(view)
        }
    }

    private fun updateList(recyclerView: RecyclerView) {
        tirthyatra?.let { yatra ->
            val displayList = yatra.participants.map { user ->
                // Check for Creator Override first
                if (user.id == yatra.creatorId?.id && yatra.creatorDetails != null) {
                    val details = yatra.creatorDetails!!
                    user.copy(
                        name = details.name ?: user.name,
                        dob = details.age ?: user.dob, // Passing Age as DOB for AgeUtils
                        gender = details.gender ?: user.gender,
                        phone = details.contact ?: user.phone
                    )
                } 
                // Check if there are specific participant details for this user (e.g. accepted member)
                else {
                    // Match user.id (which is TirthyatraUser.id) with participantDetails.userId (String)
                    // Note: participantDetails might be null
                     val pDetail = yatra.participantDetails?.find { it.userId == user.id }
                     if (pDetail != null) {
                         user.copy(
                            name = pDetail.name ?: user.name,
                            dob = pDetail.age ?: user.dob, // Passing Age as DOB for AgeUtils
                            gender = pDetail.gender ?: user.gender,
                            phone = pDetail.contact ?: user.phone
                         )
                     } else {
                         user
                     }
                }
            }

            // Display all participants
            recyclerView.adapter = MembersAdapter(displayList) { user ->
                 // Show Details Dialog
                 // Use AgeUtils for click dialog too
                 val age = com.mycompany.jainconnect.utils.AgeUtils.calculateAge(user.dob)
                 
                 val message = "Name: ${user.name}\n" +
                      "Age: $age\n" +
                      "Gender: ${user.gender ?: "N/A"}\n" +
                      "Contact: ${user.phone ?: "N/A"}" 
                      
                 androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Member Details")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    companion object {
        fun newInstance(yatra: Tirthyatra) = YatraMembersFragment().apply {
            arguments = Bundle().apply { putParcelable("YATRA_DATA", yatra) }
        }
    }
}

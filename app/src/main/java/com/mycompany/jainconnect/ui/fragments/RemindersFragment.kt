package com.mycompany.jainconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.ui.adapters.RemindersAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import com.mycompany.jainconnect.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemindersFragment : Fragment() {

    private val viewModel: JainViewModel by activityViewModels() // Share ViewModel with Activity
    private lateinit var adapter: RemindersAdapter
    private lateinit var tvEmptyState: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Notification Channel
        context?.let { NotificationHelper.createNotificationChannel(it) }

        val rvReminders = view.findViewById<RecyclerView>(R.id.recyclerViewReminders)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        adapter = RemindersAdapter(emptyList())
        rvReminders.layoutManager = LinearLayoutManager(context)
        rvReminders.adapter = adapter

        // Observe Saved Events (which acts as our "My Events / Reminders" list)
        viewModel.savedEvents.observe(viewLifecycleOwner) { events ->
            // Filter logic could go here if we mostly want upcoming events?
            // For now, show all saved.
            if (events.isNullOrEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                rvReminders.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                rvReminders.visibility = View.VISIBLE
                adapter.updateList(events)

                // SCHEDULE ALARMS FOR ALL THESE EVENTS
                // (In a real app, maybe do this diff-based so we don't re-schedule every time UI updates)
                // But AlarmManager overwrites if PendingIntent matches, so it's safe-ish.
                context?.let { ctx ->
                    events.forEach { event ->
                        NotificationHelper.scheduleEventReminder(ctx, event)
                    }
                }
            }
        }

        // Trigger fetch if not already done
        viewModel.fetchSavedEvents()
    }
}

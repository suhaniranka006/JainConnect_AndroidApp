package com.mycompany.jainconnect.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mycompany.jainconnect.R
import com.mycompany.jainconnect.data.models.ChatMessage
import com.mycompany.jainconnect.ui.adapters.ChatAdapter
import com.mycompany.jainconnect.ui.viewmodel.JainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    
    // Lazy initialization to avoid instantiation crash if dependency is missing
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    
    private val viewModel: JainViewModel by viewModels() // Use ViewModel to get User Name
    private var currentUserName = "Anonymous"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeViews(view)
            setupRecyclerView()
            fetchCurrentUserProfile()
            setupSendButton()
            listenForMessages()
            setupKeyboardListener(view)
        } catch (e: Throwable) {
            e.printStackTrace()
            Toast.makeText(context, "Error initializing chat: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews(view: View) {
        rvChat = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        val tvCharCount = view.findViewById<android.widget.TextView>(R.id.tvCharCount)

        etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                tvCharCount.text = "$length/200"
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid ?: ""
        chatAdapter = ChatAdapter(currentUserId) { message ->
            showDeleteOptionsDialog(message)
        }
        rvChat.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true // Start from bottom
        }
        rvChat.adapter = chatAdapter
    }

    // Reuse Logic from HomeFragment to get Name
    private fun fetchCurrentUserProfile() {
        val sharedPref = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            viewModel.fetchUserProfile(token)
            viewModel.userProfile.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    currentUserName = user.name
                }
            }
        }
    }

    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Optimistically clear immediately for better UX
                etMessage.text.clear()
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(text: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "You must be logged in to chat", Toast.LENGTH_SHORT).show()
            return
        }
        val timestamp = System.currentTimeMillis()

        // Create Message Object
        val chatMessage = ChatMessage(
            id = "", // Firestore will generate
            senderId = userId,
            senderName = currentUserName,
            message = text,
            timestamp = timestamp
        )

        // Add to Firestore
        firestore.collection("global_chat")
            .add(chatMessage)
            .addOnSuccessListener {
                // etMessage.text.clear() // MOVED UP
                rvChat.smoothScrollToPosition(chatAdapter.itemCount)
                
                // Trigger Notification
                triggerNotification(text)
            }
            .addOnFailureListener { e ->
                val errorMsg = if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
                    "Firestore Error: ${e.code} - ${e.message}"
                } else {
                    "Failed: ${e.message}"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                // Optionally restore text if failed
                etMessage.setText(text)
            }
    }

    private fun triggerNotification(message: String) {
        val sharedPref = requireActivity().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)

        if (token != null) {
            val title = "New Message from $currentUserName"
            viewModel.sendChatNotification(token, title, message)
        }
    }

    private fun showDeleteOptionsDialog(message: ChatMessage) {
        val options = mutableListOf<String>()
        val currentUserId = auth.currentUser?.uid
        
        // "Delete for Me" is always available
        options.add("Delete for Me")

        // "Delete for Everyone" (Sender only)
        if (currentUserId != null && message.senderId == currentUserId) {
            options.add("Delete for Everyone")
        }
        
        // "Report" (Not for my own messages)
        if (currentUserId != null && message.senderId != currentUserId) {
             options.add("Report Message")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Message Options")
            .setItems(options.toTypedArray()) { _, which ->
                val selectedOption = options[which]
                when (selectedOption) {
                    "Delete for Me" -> deleteMessageForMe(message.id)
                    "Delete for Everyone" -> deleteMessageForEveryone(message.id)
                    "Report Message" -> reportMessage(message)
                }
            }
            .show()
    }
    
    private fun reportMessage(message: ChatMessage) {
        val userId = auth.currentUser?.uid ?: return
        if (message.id.isEmpty()) return

        // Check if already reported by me locally? Ideally backend check.
        // We will just do arrayUnion, Firestore handles duplicates.
        
        firestore.collection("global_chat").document(message.id)
            .update("reportedBy", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Toast.makeText(context, "Message reported. Thank you.", Toast.LENGTH_SHORT).show()
                // If this was the 3rd report, it will disappear on next snapshot update automatically.
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    // ... deleteMessageForMe ...
    // ... deleteMessageForEveryone ...
    
    private fun deleteMessageForMe(messageId: String) {
        val sharedPref = requireActivity().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
        val deletedSet = sharedPref.getStringSet("deleted_messages", mutableSetOf()) ?: mutableSetOf()
        
        val newSet = deletedSet.toMutableSet()
        newSet.add(messageId)
        
        with(sharedPref.edit()) {
            putStringSet("deleted_messages", newSet)
            apply()
        }
        Toast.makeText(context, "Message hidden", Toast.LENGTH_SHORT).show()
        listenForMessages() 
    }

    private fun deleteMessageForEveryone(messageId: String) {
        if (messageId.isEmpty()) {
            Toast.makeText(context, "Cannot delete this message (ID missing)", Toast.LENGTH_SHORT).show()
            return
        }
        firestore.collection("global_chat").document(messageId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Message deleted for everyone", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var messageListener: com.google.firebase.firestore.ListenerRegistration? = null

    private fun listenForMessages() {
        messageListener?.remove()

        val sharedPref = requireActivity().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
        
        messageListener = firestore.collection("global_chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100) 
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val deletedSet = sharedPref.getStringSet("deleted_messages", emptySet()) ?: emptySet()
                    
                    val messages = ArrayList<ChatMessage>()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(ChatMessage::class.java)
                        if (msg != null) {
                            val msgWithId = msg.copy(id = doc.id)
                            
                            // FILTER 1: Local Delete
                            if (deletedSet.contains(msgWithId.id)) continue
                            
                            // FILTER 2: Auto-Moderation (Hide if 1+ report for testing)
                            // DEBUGGING: Show count if reported
                            if (msgWithId.reportedBy.isNotEmpty()) {
                                // android.util.Log.d("ChatDebug", "Msg ${msgWithId.message} has ${msgWithId.reportedBy.size} reports")
                            }
                            
                            if (msgWithId.reportedBy.size >= 1) continue
                            
                            messages.add(msgWithId)
                        }
                    }
                    
                    chatAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        rvChat.smoothScrollToPosition(messages.size - 1)
                    }
                    // Always mark as read based on SERVER count, even if local list is empty/filtered
                    (activity as? com.mycompany.jainconnect.ui.activities.MainActivity)?.markChatAsRead(snapshot.size())
                }
            }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        messageListener?.remove()
    }

    private fun setupKeyboardListener(view: View) {
        val activityRootView = requireActivity().findViewById<View>(android.R.id.content)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_navigation)
        
        // Listen for layout changes (keyboard open/close)
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
            // Check if view is attached
            if (!view.isAttachedToWindow) return@addOnGlobalLayoutListener
            
            val rect = android.graphics.Rect()
            activityRootView.getWindowVisibleDisplayFrame(rect)
            
            val screenHeight = activityRootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            
            // If keypad height is more than 15% of screen, it's probably open
            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is OPEN -> Hide Bottom Nav
                if(bottomNav.visibility == View.VISIBLE) {
                    bottomNav.visibility = View.GONE
                }
            } else {
                // Keyboard is CLOSED -> Show Bottom Nav
                if(bottomNav.visibility != View.VISIBLE) {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}

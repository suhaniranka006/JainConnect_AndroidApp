package com.mycompany.jainconnect.data.models

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val reportedBy: List<String> = emptyList()
)

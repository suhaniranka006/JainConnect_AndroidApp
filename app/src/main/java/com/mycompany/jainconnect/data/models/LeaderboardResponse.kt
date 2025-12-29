
package com.mycompany.jainconnect.data.models

data class LeaderboardResponse(
    val success: Boolean,
    val data: List<LeaderboardUser>
)

data class LeaderboardUser(
    val _id: String,
    val name: String,
    val profileImage: String?,
    val coins: Int
)

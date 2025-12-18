package com.mycompany.jainconnect.data.models

import com.google.gson.annotations.SerializedName

data class TemplateListResponse(
    val success: Boolean,
    val data: List<TirthyatraTemplate>
)

data class YatraListResponse(
    val success: Boolean,
    val count: Int,
    val data: List<Tirthyatra>
)

data class SingleYatraResponse(
    val success: Boolean,
    val data: Tirthyatra
)

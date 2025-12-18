package com.mycompany.jainconnect.data.repository

import com.mycompany.jainconnect.data.models.ApiResponse
import com.mycompany.jainconnect.data.models.TemplateListResponse
import com.mycompany.jainconnect.data.models.YatraListResponse
import com.mycompany.jainconnect.data.models.SingleYatraResponse
import com.mycompany.jainconnect.data.models.Tirthyatra
import com.mycompany.jainconnect.data.network.ApiService
import com.mycompany.jainconnect.data.network.NetworkResult
import retrofit2.Response
import javax.inject.Inject

class TirthyatraRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTemplates(isPopular: Boolean?): NetworkResult<TemplateListResponse> {
        return handleApi { apiService.getTirthyatraTemplates(isPopular) }
    }

    suspend fun createYatra(token: String, yatra: Tirthyatra): NetworkResult<SingleYatraResponse> {
        return handleApi { apiService.createYatra("Bearer $token", yatra) }
    }

    suspend fun getMyYatras(token: String): NetworkResult<YatraListResponse> {
        return handleApi { apiService.getMyYatras("Bearer $token") }
    }

    suspend fun getPublicYatras(token: String): NetworkResult<YatraListResponse> {
        return handleApi { apiService.getMyYatras("Bearer $token", "public") }
    }

    suspend fun getYatraDetails(token: String, id: String): NetworkResult<SingleYatraResponse> {
        return handleApi { apiService.getYatraDetails("Bearer $token", id) }
    }

    suspend fun joinYatra(token: String, id: String, message: String, contactNumber: String): NetworkResult<ApiResponse> {
        val body = mapOf("message" to message, "contactNumber" to contactNumber)
        return handleApi { apiService.joinYatra("Bearer $token", id, body) }
    }

    suspend fun cancelRequest(token: String, id: String): NetworkResult<ApiResponse> {
        return handleApi { apiService.cancelRequest("Bearer $token", id) }
    }

    suspend fun toggleCompanionship(token: String, id: String, enable: Boolean): NetworkResult<SingleYatraResponse> {
        val body = mapOf("enable" to enable)
        return handleApi { apiService.toggleCompanionship("Bearer $token", id, body) }
    }

    suspend fun manageMember(token: String, id: String, targetUserId: String, action: String): NetworkResult<SingleYatraResponse> {
        val body = mapOf("targetUserId" to targetUserId, "action" to action)
        return handleApi { apiService.manageMember("Bearer $token", id, body) }
    }

    suspend fun deleteYatra(token: String, id: String): NetworkResult<ApiResponse> {
        return handleApi { apiService.deleteYatra("Bearer $token", id) }
    }



    private suspend fun <T> handleApi(
        execute: suspend () -> Response<T>
    ): NetworkResult<T> {
        return try {
            val response = execute()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Code: ${response.code()} Message: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown Error")
        }
    }
}

package com.mzansiready.app.data.remote

import com.mzansiready.app.data.remote.dto.AlertDto
import com.mzansiready.app.data.remote.dto.AuthResponse
import com.mzansiready.app.data.remote.dto.CreateLocationRequest
import com.mzansiready.app.data.remote.dto.LocationDto
import com.mzansiready.app.data.remote.dto.LoginRequest
import com.mzansiready.app.data.remote.dto.RegisterRequest
import com.mzansiready.app.data.remote.dto.SettingsDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/users/settings")
    suspend fun getSettings(@Header("Authorization") bearer: String): Response<SettingsDto>

    @PUT("api/users/settings")
    suspend fun updateSettings(
        @Header("Authorization") bearer: String,
        @Body body: SettingsDto
    ): Response<SettingsDto>

    @GET("api/locations")
    suspend fun getLocations(@Header("Authorization") bearer: String): Response<List<LocationDto>>

    @POST("api/locations")
    suspend fun createLocation(
        @Header("Authorization") bearer: String,
        @Body body: CreateLocationRequest
    ): Response<LocationDto>

    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(
        @Header("Authorization") bearer: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/alerts")
    suspend fun getAlerts(@Header("Authorization") bearer: String): Response<List<AlertDto>>
}
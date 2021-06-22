package com.example.geofencingalerts.service
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface IApiSmsRestService {

    @POST("sms")
    suspend fun postSms(
        @Query("Body") Body: String,
        @Query("To") To: String,
        @Query("From") From: String
    ): Response<String>
}
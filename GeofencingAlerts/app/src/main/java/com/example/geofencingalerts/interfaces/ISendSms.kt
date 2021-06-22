package com.example.geofencingalerts.interfaces

import android.content.Context

interface ISendSms {

    @Override
    fun sendSms(cellPhone: String, message: String, context: Context)
}
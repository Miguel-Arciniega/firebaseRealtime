package com.example.u4_sms.dao

import com.example.u4_sms.model.SmsMessageModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DataBaseHelper {

    val databaseReference =
        Firebase.database.getReference("SmsMessage")

    /**
     *  Añadir SmsMessage a la base de datos
     */
    fun addOne(smsMessageModel : SmsMessageModel) {

        databaseReference.push().setValue(smsMessageModel)
    }
}
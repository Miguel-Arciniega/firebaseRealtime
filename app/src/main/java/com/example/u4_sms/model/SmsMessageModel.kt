package com.example.u4_sms.model

class SmsMessageModel (var phoneNumber: String? = null,
                       var dateTime: String? = null,
                       var message: String? = null) {

    override fun toString(): String {
        return  "Numero de telefono: $phoneNumber\n" +
                "Fecha: $dateTime\n" +
                "Mensaje: $message\n"
    }
}
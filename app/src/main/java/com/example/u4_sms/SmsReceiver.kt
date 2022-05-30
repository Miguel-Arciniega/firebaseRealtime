package com.example.u4_sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast
import com.example.u4_sms.dao.DataBaseHelper
import com.example.u4_sms.model.SmsMessageModel
import java.text.SimpleDateFormat
import java.util.*

class SmsReceiver : BroadcastReceiver() {

    private val dataBaseHelper = DataBaseHelper()

    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras

        if(extras != null) {
            val sms = extras.get("pdus") as Array<*>

            for (indice in sms.indices) {
                val formato = extras.getString("format")

                val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray, formato)
                } else {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                val smsOrigin = smsMessage.originatingAddress
                val smsContent = smsMessage.messageBody.toString()
                val milliseconds = smsMessage.timestampMillis
                val smsDateTime = getDate(milliseconds, "dd/MM/yyyy hh:mm:ss.SSS")


                val smsMessageModel = SmsMessageModel(smsOrigin, smsDateTime, smsContent)

                dataBaseHelper.addOne(smsMessageModel)

                Toast.makeText(
                    context,
                    "$smsMessageModel",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

}
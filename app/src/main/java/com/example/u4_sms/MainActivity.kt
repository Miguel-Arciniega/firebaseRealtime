package com.example.u4_sms

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.u4_sms.dao.DataBaseHelper
import com.example.u4_sms.databinding.ActivityMainBinding
import com.example.u4_sms.model.SmsMessageModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseHelper : DataBaseHelper
    private val permissionReceiveValue = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val listViewDatabase = binding.listView
        val listViewSeleccionados = binding.listview2

        setContentView(binding.root)

        databaseHelper = DataBaseHelper()

        val query = databaseHelper.databaseReference
        val smsMessageListEnLaBaseDeDatos = ArrayList<SmsMessageModel>()
        val smsMessageListSeleccionados = ArrayList<SmsMessageModel>()

        val databaseListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                smsMessageListEnLaBaseDeDatos.clear()
                for (data in snapshot.children){
                    val smsMessage = data.getValue<SmsMessageModel>()!!
                    smsMessageListEnLaBaseDeDatos.add(smsMessage)
                }
                updateListView(smsMessageListEnLaBaseDeDatos, listViewDatabase)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,
                    "Error al obtener datos de la base de datos",
                    Toast.LENGTH_LONG).show()
            }
        }
        query.addValueEventListener(databaseListener)

        if (ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),
                permissionReceiveValue
            )
        }

        binding.button.setOnClickListener {
            if (smsMessageListSeleccionados.isNotEmpty()){
                createCsvDocument(smsMessageListSeleccionados)
                smsMessageListSeleccionados.clear()
                updateListView(smsMessageListSeleccionados, listViewSeleccionados)

                // Obtenemos el file del path
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                )

                val fileName = "smsMessageList.csv"
                val file = File(path, "/$fileName")

                Toast.makeText(this,
                    "Archivo descargado en: \n$file",
                    Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this,
                    "No hay datos que descargar",
                    Toast.LENGTH_LONG).show()
            }
        }

        binding.button2.setOnClickListener {
            try {
                openCsvDocument()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al abrir el archivo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.button3.setOnClickListener{
            if (smsMessageListEnLaBaseDeDatos.isNotEmpty()){
                createCsvDocument(smsMessageListEnLaBaseDeDatos)

                // Obtenemos el file del path
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS
                )

                val fileName = "smsMessageList.csv"
                val file = File(path, "/$fileName")

                Toast.makeText(this,
                    "Archivo descargado en: \n$file",
                    Toast.LENGTH_LONG).show()
            } else{
                Toast.makeText(this,
                    "No hay datos que descargar",
                    Toast.LENGTH_LONG).show()
            }
        }

        listViewDatabase.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val smsMessageModel = listViewDatabase.getItemAtPosition(position) as SmsMessageModel

                // Construimos un AlertDialog
                android.app.AlertDialog.Builder(this)

                    // Añadimos el titulo y el mensaje al AlertDialog
                    .setTitle("Aviso")
                    .setMessage("Desea seleccionar este mensaje? \n\n$smsMessageModel")

                    // Si se hace click en el boton de ELIMINAR, processamos la acción
                    .setNegativeButton("Aceptar") { _, _ ->
                        var isAlreadyInTheList = false
                        for (smsMessage in smsMessageListSeleccionados ){
                            if (smsMessage == smsMessageModel){
                                isAlreadyInTheList = true
                            }
                        }

                        if (isAlreadyInTheList) {
                            Toast.makeText(this,
                                "Este mensaje ya está seleccionado",
                                Toast.LENGTH_LONG).show()
                        }else {
                            smsMessageListSeleccionados.add(smsMessageModel)
                            updateListView(smsMessageListSeleccionados, listViewSeleccionados)
                        }
                    }

                    // Si se hace click en el boton de CANCELAR, processamos la acción
                    .setNeutralButton("Cancelar") { _, _ ->

                    }.show()
        }

        listViewSeleccionados.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val smsMessageModel = listViewSeleccionados.getItemAtPosition(position) as SmsMessageModel

                // Construimos un AlertDialog
                android.app.AlertDialog.Builder(this)

                    // Añadimos el titulo y el mensaje al AlertDialog
                    .setTitle("Aviso")
                    .setMessage("Desea eliminar este mensaje de los archivos seleccionados? \n\n$smsMessageModel")

                    // Si se hace click en el boton de ELIMINAR, processamos la acción
                    .setNegativeButton("Aceptar") { _, _ ->
                        smsMessageListSeleccionados.removeAt(position)
                        updateListView(smsMessageListSeleccionados, listViewSeleccionados)
                    }

                    // Si se hace click en el boton de CANCELAR, processamos la acción
                    .setNeutralButton("Cancelar") { _, _ ->

                    }.show()
            }
    }

    private fun openCsvDocument() {
        try {

            // Obtenemos el file del path
            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )

            val fileName = "smsMessageList.csv"
            val file = File(path, "/$fileName")

            // Abrimos el archivo
            val csvIntent = Intent(Intent.ACTION_VIEW)
            csvIntent.setDataAndType(
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file),
                "text/csv"
            )

            csvIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            csvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(csvIntent)
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Aplicación no encontrada. Asegurese de tener excel instalado", Toast.LENGTH_LONG).show()
            e.printStackTrace()

        }
    }

    private fun createCsvDocument(smsMessageList : ArrayList<SmsMessageModel>) {
        val path = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS
        )

        val fileName = "smsMessageList.csv"
        val file = File(path, "/$fileName")

        writeToCsvFile(file, smsMessageList)
    }

    private fun writeToCsvFile(file : File, smsMessageList : ArrayList<SmsMessageModel>) {
        val writer = BufferedWriter(file.bufferedWriter())

        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("Numero de telefono", "Fecha", "Mensaje"));

        for (smsMessage in smsMessageList) {
            val smsMessageData = listOf(
                smsMessage.phoneNumber.toString(),
                smsMessage.dateTime.toString(),
                smsMessage.message.toString()
            )
            csvPrinter.printRecord(smsMessageData)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    private fun updateListView(smsMessageList: ArrayList<SmsMessageModel>, listView: ListView) {
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, smsMessageList)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionReceiveValue){
            receiveMessage()
        }
    }

    private fun receiveMessage() {
        AlertDialog.Builder(this)
            .setMessage("SE OTORGO RECIBIR")
    }
}
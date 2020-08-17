package com.bzrudski.nuptiallog.management

import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat

object FlightAppManager {
    val dateFormatter = SimpleDateFormat()
    val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // from https://stackoverflow.com/questions/54683532/serializing-deserializing-simple-object-using-gson-no-time-zone-indicator-erro
        .create()
}
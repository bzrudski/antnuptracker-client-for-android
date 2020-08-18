package com.bzrudski.nuptiallog.models.users

import android.os.Build

object Device {

    const val DEVICE_ID_KEY = "USER_DEVICE_ID"

    const val platform = "ANDROID"
    val model = "${Build.MANUFACTURER} ${Build.MODEL}"
    var deviceID: Int = 0
    var deviceToken:String? = null

    fun getInfoForDevice(): Map<String, String?>{
        val deviceInfoMap = HashMap<String, String?>()
        deviceInfoMap["platform"] = platform
        deviceInfoMap["model"] = model

        return deviceInfoMap
    }
}
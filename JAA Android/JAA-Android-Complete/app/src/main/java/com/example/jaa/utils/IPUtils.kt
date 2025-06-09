package com.example.jaa.utils

import android.os.Build

object IPUtils {
    fun getBaseUrl(): String {
        return if (Build.FINGERPRINT.contains("generic")) {
            "http://10.0.2.2:5001/"
        } else {
            "http://192.168.1.25:5001/"
        }
    }
}
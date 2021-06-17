package com.dazone.crewchatoff.retrofit

import android.content.Context
import com.dazone.crewchatoff.dto.ChattingDto
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Response
import java.lang.Exception

object Utils {
    fun downloadFileFromUrl(context: Context, url: String, fileName: String) {

    }
}

sealed class DownloadResult {
    object Success : DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Int): DownloadResult()
}


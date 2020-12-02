package com.dazone.crewchatoff.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

fun downloadFile(url: String, context: Context, name: String) {
    val request = DownloadManager.Request(Uri.parse(url))
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
    request.setTitle("Download")
    request.setDescription("Downloading $name ...")
    request.allowScanningByMediaScanner()
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, System.currentTimeMillis().toString())
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    manager.enqueue(request)
}
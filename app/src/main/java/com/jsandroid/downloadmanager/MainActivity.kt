package com.jsandroid.downloadmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import android.widget.Toast
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var downloadId: Long = -1L
    private lateinit var downloadManager: DownloadManager

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.action)) {
                if (downloadId == id) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(id)
                    var cursor = downloadManager.query(query)
                    if (!cursor.moveToFirst()) {
                        return
                    }

                    var columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    var status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download succeeded", Toast.LENGTH_SHORT).show()
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.action)) {
                Toast.makeText(context, "Notification clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val intentFilter = IntentFilter()
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        registerReceiver(onDownloadComplete, intentFilter)

        downloadBtn.setOnClickListener {
            downloadImage()
        }

        statusBtn.setOnClickListener {
            val status = getStatus(downloadId)
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        }

        cancelBtn.setOnClickListener {
            if (downloadId != -1L) {
                downloadManager.remove(downloadId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    private fun downloadImage() {
        val file = File(getExternalFilesDir(null), "dev_submit.mp4")
        val youtubeUrl = "https://r2---sn-oguelney.googlevideo.com/videoplayback?expire=1547361698&ratebypass=yes&ipbits=0&txp=5431432&fvip=2&sparams=clen,dur,ei,expire,gir,id,ip,ipbits,itag,lmt,mime,mip,mm,mn,ms,mv,pl,ratebypass,requiressl,source,usequic&dur=2487.205&source=youtube&id=o-AFU5WYmppmSyvhuN-vYHnA9zb_qazPL5JANaBNepI9ZF&requiressl=yes&lmt=1541762056298111&itag=18&ip=52.78.151.237&clen=106473489&signature=02FE1AD7C3C0FDFB173383A9D48A1374FDDE9470.31A7D7D3F98045946B0DD3C07436A241B223B84D&ei=Qok6XOqPGI3DqQHxlL_4CA&pl=24&key=cms1&c=WEB&gir=yes&mime=video%2Fmp4&redirect_counter=1&cm2rm=sn-oguy67l&req_id=471900e67ee3a3ee&cms_redirect=yes&mip=182.228.195.55&mm=34&mn=sn-oguelney&ms=ltu&mt=1547339611&mv=u&usequic=no"
        val request = DownloadManager.Request(Uri.parse(youtubeUrl))
            .setTitle("Downloading a video")
            .setDescription("Downloading Dev Summit")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        Log.d(TAG, "path : " + file.path)
    }

    private fun getStatus(id: Long): String {
        val query: DownloadManager.Query = DownloadManager.Query()
        query.setFilterById(id)
        var cursor = downloadManager.query(query)
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Empty row")
            return "Wrong downloadId"
        }

        var columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        var status = cursor.getInt(columnIndex)
        var columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        var reason = cursor.getInt(columnReason)
        var statusText: String

        when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> statusText = "Successful"
            DownloadManager.STATUS_FAILED -> {
                statusText = "Failed: $reason"
            }
            DownloadManager.STATUS_PENDING -> statusText = "Pending"
            DownloadManager.STATUS_RUNNING -> statusText = "Running"
            DownloadManager.STATUS_PAUSED-> {
                statusText = "Paused: $reason"
            }
            else -> statusText = "Unknown"
        }

        return statusText
    }
}

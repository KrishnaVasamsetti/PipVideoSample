package com.mobile.pipvideosample

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mobile.pipvideosample.databinding.ActivityMainBinding
import kotlin.time.Duration.Companion.minutes


class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding
    private val visibleRect = Rect()

    private val isSupportsPIP by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }

//    private val VIDEO_PLAYBACK_URL = "https://file-examples.com/storage/fe352586866388d59a8918d/2017/04/file_example_MP4_480_1_5MG.mp4"

        private val VIDEO_PLAYBACK_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    var videoUrl =
        "https://media.geeksforgeeks.org/wp-content/uploads/20201217192146/Screenrecorder-2020-12-17-19-17-36-828.mp4?_=1"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

//        dataBinding.videoView.setVideoURI(Uri.parse(VIDEO_PLAYBACK_URL))
        val path = "android.resource://" + packageName + "/" + R.raw.globe_motion
        dataBinding.videoView.setVideoURI(Uri.parse(VIDEO_PLAYBACK_URL))
//        dataBinding.videoView.setVideoURI(Uri.parse(videoUrl))
//        dataBinding.videoView.setVideoURI(Uri.parse(path))

        val mediaController = MediaController(this)
        mediaController.setAnchorView(dataBinding.videoView)
        mediaController.setMediaPlayer(dataBinding.videoView)

        dataBinding.videoView.setMediaController(mediaController)
        dataBinding.videoView.start()

        dataBinding.videoView.getGlobalVisibleRect(visibleRect)

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (!isSupportsPIP) {
            return
        }

        setPipMode()
    }

    private fun setPipMode() {
        updatePipParams()?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(it)
            }
        }
    }

    private fun updatePipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setSourceRectHint(visibleRect)
                .setAspectRatio(Rational(16, 9))
                .build()
        } else {
            return null
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        } else return

        if (isInPictureInPictureMode) {
            dataBinding.titleTextView.isVisible = false
            supportActionBar?.hide()
        } else {
            dataBinding.titleTextView.isVisible = true
            supportActionBar?.show()
        }
    }
}
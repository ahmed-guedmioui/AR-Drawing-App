package com.ardrawing.sketchtrace.sketch.data.repository

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.ardrawing.sketchtrace.sketch.domain.repository.SketchRepository
import com.ardrawing.sketchtrace.sketch.presentation.util.SketchBitmap
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.common.TrackStatus
import com.otaliastudios.transcoder.source.UriDataSource
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.validator.DefaultValidator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class SketchRepositoryImpl @Inject constructor(
    private val application: Application
) : SketchRepository {

    private val photosFolderName = "AR Drawing Photos"
    private val videosFolderName = "AR Drawing Videos"

    private fun notifyMediaScanner(file: File, isVideo: Boolean) {

        if (isVideo) {
            try {
                MediaScannerConnection.scanFile(
                    application, arrayOf(file.path), arrayOf("video/mp4")
                ) { _, uri ->

                    val mediaScanIntent = Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri
                    )
                    application.sendBroadcast(mediaScanIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            try {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                Log.d("Tag_scan", contentUri.toString())
                application.sendBroadcast(mediaScanIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override suspend fun savePhoto(): Boolean {
        if (SketchBitmap.bitmapToSave == null) {
            return false
        }

        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())
        val fileName = "image_$timestamp.png"

        return try {
            withContext(Dispatchers.IO) {
                val picturesFolder = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    photosFolderName
                )

                if (!picturesFolder.exists()) {
                    picturesFolder.mkdirs()
                }

                val photoFile = File(picturesFolder, fileName)

                FileOutputStream(photoFile).use { fileOutputStream ->
                    val byteArray = ByteArrayOutputStream()
                    SketchBitmap.bitmapToSave?.compress(
                        Bitmap.CompressFormat.PNG, 100, byteArray
                    )
                    fileOutputStream.write(byteArray.toByteArray())
                    fileOutputStream.close()
                }

                notifyMediaScanner(photoFile, false)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    override suspend fun saveVideo(
        file: File, isFast: Boolean
    ): Boolean {

        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())
        val fileNameOutput = "video_$timestamp.mp4"

        return try {
            withContext(Dispatchers.IO) {
                val appVideosDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    videosFolderName
                )

                if (!appVideosDir.exists()) {
                    appVideosDir.mkdirs()
                }

                val outputFile = File(appVideosDir, fileNameOutput)
                val outputStream = FileOutputStream(outputFile)

                if (!isFast) {
                    outputStream.write(file.readBytes())
                    outputStream.close()
                    notifyMediaScanner(outputFile, true)
                    true
                } else {
                    val deferred = CompletableDeferred<Boolean>()

                    speedUpVideo(file, fileNameOutput) { transcodeOutputFile ->
                        val isSaved = if (transcodeOutputFile != null) {
                            outputStream.write(transcodeOutputFile.readBytes())
                            outputStream.close()
                            notifyMediaScanner(outputFile, true)
                            true
                        } else {
                            false
                        }

                        // Complete the deferred with the result
                        deferred.complete(isSaved)
                    }

                    // Wait for the result from the deferred
                    deferred.await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun speedUpVideo(
        videoToSpeedUp: File,
        fileNameOutput: String,
        onVideoFinished: (transcodeOutputFile: File?) -> Unit
    ) {
        try {

            val outputDir = File(application.getExternalFilesDir(null), "outputs")
            outputDir.mkdir()
            val transcodeOutputFile = File.createTempFile(fileNameOutput, ".mp4", outputDir)

            val transcodeAudioStrategy =
                DefaultAudioStrategy.builder().channels(DefaultAudioStrategy.CHANNELS_AS_INPUT)
                    .sampleRate(DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT).build()

            val transcodeVideoStrategy = DefaultVideoStrategy.Builder().frameRate(30).build()

            val builder = Transcoder.into(transcodeOutputFile.absolutePath)
            val source = UriDataSource(application, videoToSpeedUp.toUri())

            builder.setListener(object : TranscoderListener {
                override fun onTranscodeProgress(progress: Double) {
                    Log.d("tag_speed", "onTranscodeProgress: $progress")
                }

                override fun onTranscodeCompleted(successCode: Int) {
                    onVideoFinished(transcodeOutputFile)
                }

                override fun onTranscodeCanceled() {
                    onVideoFinished(null)
                }

                override fun onTranscodeFailed(exception: Throwable) {
                    onVideoFinished(null)
                }
            }).addDataSource(source).setAudioTrackStrategy(transcodeAudioStrategy)
                .setVideoTrackStrategy(transcodeVideoStrategy)
                .setValidator(object : DefaultValidator() {
                    override fun validate(
                        videoStatus: TrackStatus, audioStatus: TrackStatus
                    ): Boolean {
                        return super.validate(videoStatus, audioStatus)
                    }
                }).setSpeed(1.7f).transcode()

        } catch (e: Exception) {
            e.printStackTrace()
            onVideoFinished(null)
        }
    }


    override suspend fun deleteTempVideo(uri: String): Boolean {
        val file = Uri.parse(uri).path?.let { File(it) }

        if (file != null) {
            if (file.exists()) {
                file.delete()
                return true
            }
        }

        return false
    }

}



















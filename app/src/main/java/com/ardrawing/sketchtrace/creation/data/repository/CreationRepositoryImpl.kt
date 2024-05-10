package com.ardrawing.sketchtrace.creation.data.repository

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.ardrawing.sketchtrace.creation.domian.model.Creation
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class CreationRepositoryImpl @Inject constructor(
    private val application: Application
) : CreationRepository {

    private val photosFolderName = "AR Drawing Photos"
    private val videosFolderName = "AR Drawing Videos"

    override suspend fun deleteCreation(uri: String): Boolean {
        try {
            Log.d("TAG_CREATION", "uri: $uri")
            val contentUri = Uri.parse(uri)
            val contentResolver = application.contentResolver

            // Delete the media file using the content resolver
            contentResolver.delete(contentUri, null, null)

            // Notify media scanner about the deletion
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = contentUri
            application.sendBroadcast(mediaScanIntent)

            // Get the file path from the content URI
            val filePath = getFilePathFromContentUri(contentUri)

            // Check if the file path is not null and delete the file
            return if (filePath != null) {
                val fileToDelete = File(filePath)
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                    Log.d("TAG_CREATION", "delete")

                    true
                } else {
                    Log.d("TAG_CREATION", "!exists")

                    false
                }
            } else {
                Log.d("TAG_CREATION", "filePath null")

                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAG_CREATION", "Exception: ${e.stackTrace}")

            return false
        }
    }

    private fun getFilePathFromContentUri(contentUri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = application.contentResolver.query(
            contentUri, projection, null, null, null
        )
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath
    }

    override suspend fun getCreationList(): Flow<List<Creation>> {

        return flow {

            val creationList = mutableListOf<Creation>()

            // Retrieve URIs for saved images
            val imagesCollection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val imageProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val imageSelection = "${MediaStore.Images.Media.DATA} LIKE ?"
            val imageSelectionArgs = arrayOf(
                "%$photosFolderName%"
            )

            application.contentResolver.query(
                imagesCollection, imageProjection, imageSelection, imageSelectionArgs, null
            )?.use { imageCursor ->
                val idColumn = imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (imageCursor.moveToNext()) {
                    val imageId = imageCursor.getLong(idColumn)
                    val imageUri = Uri.withAppendedPath(imagesCollection, imageId.toString())

                    creationList.add(
                        Creation(
                            uri = imageUri, isVideo = false
                        )
                    )
                }
            }

            // Retrieve URIs for saved videos
            val videosCollection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val videoProjection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA)
            val videoSelection = "${MediaStore.Video.Media.DATA} LIKE ?"
            val videoSelectionArgs = arrayOf(
                "%$videosFolderName%"
            )

            application.contentResolver.query(
                videosCollection, videoProjection, videoSelection, videoSelectionArgs, null
            )?.use { videoCursor ->
                val idColumn = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                while (videoCursor.moveToNext()) {
                    val videoId = videoCursor.getLong(idColumn)
                    val videoUri = Uri.withAppendedPath(videosCollection, videoId.toString())

                    creationList.add(
                        Creation(
                            uri = videoUri, isVideo = true
                        )
                    )
                }
            }

            creationList.reverse()
            emit(creationList)
        }

    }
}



















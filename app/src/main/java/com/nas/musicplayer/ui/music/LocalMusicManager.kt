package com.nas.musicplayer.ui.music

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.net.Uri
import android.util.Log
import com.nas.musicplayer.Song

object LocalMusicManager {
    private const val TAG = "LocalMusicManager"

    fun getAllAudioFiles(context: Context): List<Song> {
        val songList = mutableListOf<Song>()
        
        // VOLUME_EXTERNAL_CONTENT_URI를 명시적으로 사용하여 모든 외부 저장소 스캔
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        // IS_MUSIC 조건이 에뮬레이터에 따라 다를 수 있으므로 폭넓게 쿼리
        val selection = "${MediaStore.Audio.Media.DURATION} >= 1000" // 1초 이상의 오디오만
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        Log.d(TAG, "Starting media scan...")

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            Log.d(TAG, "Found ${cursor.count} audio files in MediaStore.")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: cursor.getString(displayNameColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
                
                val artUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), albumId
                ).toString()

                Log.d(TAG, "Loaded song: $title by $artist")

                songList.add(
                    Song(
                        id = id,
                        name = title,
                        artist = artist,
                        albumName = album,
                        streamUrl = contentUri.toString(),
                        metaPoster = artUri,
                        isDir = false
                    )
                )
            }
        } ?: Log.e(TAG, "Cursor is null, could not query MediaStore.")
        
        return songList
    }
}

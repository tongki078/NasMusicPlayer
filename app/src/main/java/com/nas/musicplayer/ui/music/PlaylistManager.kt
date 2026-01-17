package com.nas.musicplayer.ui.music

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object PlaylistManager {
    private var dao: PlaylistDao? = null
    private var repository: MusicRepository? = null

    fun init(context: Context) {
        if (dao == null) {
            val db = AppDatabase.getDatabase(context)
            dao = db.playlistDao()
            repository = MusicRepository(dao!!)
        }
    }

    val playlists: Flow<List<PlaylistEntity>> by lazy {
        dao?.getAllPlaylists() ?: emptyFlow()
    }

    suspend fun createPlaylist(name: String): Int {
        return repository?.createPlaylist(name)?.toInt() ?: 0
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        repository?.addSongToPlaylist(playlistId, song)
    }
    
    suspend fun deletePlaylistById(id: Int) {
         repository?.deletePlaylistById(id)
    }
}

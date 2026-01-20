package com.nas.musicplayer

import android.content.Context
import com.nas.musicplayer.Song
import com.nas.musicplayer.db.PlaylistDao
import com.nas.musicplayer.db.PlaylistEntity
import com.nas.musicplayer.db.getDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object PlaylistManager {
    private var dao: PlaylistDao? = null
    private var repository: MusicRepository? = null

    fun init(context: Context) {
        if (dao == null) {
            val db = getDatabase(context)
            dao = db.playlistDao()
            // 에러 수정: recentSearchDao를 함께 전달해야 합니다.
            repository = MusicRepository(dao!!, db.recentSearchDao())
        }
    }

    val playlists: Flow<List<PlaylistEntity>> by lazy {
        dao?.getAllPlaylists() ?: emptyFlow()
    }

    suspend fun createPlaylist(name: String): Long {
        return repository?.createPlaylist(name) ?: 0
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        repository?.addSongToPlaylist(playlistId, song)
    }
    
    suspend fun deletePlaylistById(id: Int) {
         repository?.deletePlaylistById(id)
    }
}

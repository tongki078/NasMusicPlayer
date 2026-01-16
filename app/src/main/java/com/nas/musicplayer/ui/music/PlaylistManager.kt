package com.nas.musicplayer.ui.music

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object PlaylistManager {
    private var dao: PlaylistDao? = null

    fun init(context: Context) {
        if (dao == null) {
            dao = AppDatabase.getDatabase(context).playlistDao()
        }
    }

    val playlists: Flow<List<PlaylistEntity>> by lazy {
        dao?.getAllPlaylists() ?: emptyFlow()
    }

    suspend fun createPlaylist(name: String): Int {
        val newPlaylist = PlaylistEntity(name = name)
        return dao?.insertPlaylist(newPlaylist)?.toInt() ?: 0
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        val playlist = dao?.getPlaylistById(playlistId) ?: return
        if (playlist.songs.none { it.id == song.id }) {
            val updatedSongs = playlist.songs.toMutableList().apply { add(song) }
            dao?.updatePlaylist(playlist.copy(songs = updatedSongs))
        }
    }
    
    suspend fun deletePlaylist(id: Int) {
         val playlist = dao?.getPlaylistById(id) ?: return
         dao?.deletePlaylist(playlist)
    }
}

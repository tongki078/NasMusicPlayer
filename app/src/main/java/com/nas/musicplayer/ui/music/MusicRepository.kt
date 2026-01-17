package com.nas.musicplayer.ui.music

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val playlistDao: PlaylistDao,
    private val recentSearchDao: RecentSearchDao
) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val recentSearches: Flow<List<RecentSearch>> = recentSearchDao.getRecentSearches()

    val allSongs: Flow<List<Song>> = allPlaylists.map { playlists ->
        playlists.flatMap { it.songs }.distinctBy { it.id }
    }

    suspend fun createPlaylist(name: String): Long {
        val newPlaylist = PlaylistEntity(name = name, songs = emptyList())
        return playlistDao.insertPlaylist(newPlaylist)
    }

    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        if (playlist.songs.none { it.id == song.id }) {
            val updatedSongs = playlist.songs.toMutableList().apply { add(song) }
            playlistDao.updatePlaylist(playlist.copy(songs = updatedSongs))
        }
    }

    suspend fun deletePlaylistById(playlistId: Int) {
        playlistDao.getPlaylistById(playlistId)?.let {
            playlistDao.deletePlaylist(it)
        }
    }

    suspend fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        recentSearchDao.insertSearch(RecentSearch(query))
    }

    suspend fun deleteRecentSearch(query: String) {
        recentSearchDao.deleteSearch(query)
    }
}

package com.nas.musicplayer.ui.music

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 데이터베이스(Room)와 상호작용하여 플레이리스트 데이터를 관리하는 클래스입니다.
 * 모든 데이터 관련 로직은 이 Repository를 통해 처리됩니다.
 */
class MusicRepository(private val playlistDao: PlaylistDao) {

    // DB에 저장된 모든 플레이리스트 목록을 실시간(Flow)으로 가져옵니다.
    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    /**
     * 기존 ViewModel과의 호환성을 위해, 모든 플레이리스트에 포함된
     * 모든 노래 목록을 중복 없이 가져옵니다.
     * 참고: 이 코드는 현재 앱 구조상 필요하지만,
     * 실제로는 각 플레이리스트 화면에서 해당 플레이리스트의 노래만 가져오는 것이 더 효율적입니다.
     */
    val allSongs: Flow<List<Song>> = allPlaylists.map { playlists ->
        playlists.flatMap { it.songs }.distinctBy { it.id }
    }

    /**
     * 새 플레이리스트를 생성합니다.
     * @param name 생성할 플레이리스트의 이름
     * @return 새로 생성된 플레이리스트의 ID
     */
    suspend fun createPlaylist(name: String): Long {
        val newPlaylist = PlaylistEntity(name = name, songs = emptyList())
        return playlistDao.insertPlaylist(newPlaylist)
    }

    /**
     * 특정 플레이리스트에 노래를 추가합니다.
     * @param playlistId 노래를 추가할 플레이리스트의 ID
     * @param song 추가할 노래 객체
     */
    suspend fun addSongToPlaylist(playlistId: Int, song: Song) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        // 중복된 노래가 없을 경우에만 추가
        if (playlist.songs.none { it.id == song.id }) {
            val updatedSongs = playlist.songs.toMutableList().apply { add(song) }
            playlistDao.updatePlaylist(playlist.copy(songs = updatedSongs))
        }
    }

    /**
     * ID를 사용하여 특정 플레이리스트를 삭제합니다.
     * @param playlistId 삭제할 플레이리스트의 ID
     */
    suspend fun deletePlaylistById(playlistId: Int) {
        playlistDao.getPlaylistById(playlistId)?.let {
            playlistDao.deletePlaylist(it)
        }
    }
}
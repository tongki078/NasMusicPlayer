package com.nas.musicplayer.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.nas.musicplayer.Song
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PlaylistDao_Impl(
  __db: RoomDatabase,
) : PlaylistDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlaylistEntity: EntityInsertAdapter<PlaylistEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfPlaylistEntity: EntityDeleteOrUpdateAdapter<PlaylistEntity>

  private val __updateAdapterOfPlaylistEntity: EntityDeleteOrUpdateAdapter<PlaylistEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPlaylistEntity = object : EntityInsertAdapter<PlaylistEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `playlists` (`id`,`name`,`songs`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        val _tmp: String = __converters.fromList(entity.songs)
        statement.bindText(3, _tmp)
      }
    }
    this.__deleteAdapterOfPlaylistEntity = object : EntityDeleteOrUpdateAdapter<PlaylistEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `playlists` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistEntity) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfPlaylistEntity = object : EntityDeleteOrUpdateAdapter<PlaylistEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `playlists` SET `id` = ?,`name` = ?,`songs` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        val _tmp: String = __converters.fromList(entity.songs)
        statement.bindText(3, _tmp)
        statement.bindLong(4, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertPlaylist(playlist: PlaylistEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlaylistEntity.insertAndReturnId(_connection, playlist)
    _result
  }

  public override suspend fun deletePlaylist(playlist: PlaylistEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfPlaylistEntity.handle(_connection, playlist)
  }

  public override suspend fun updatePlaylist(playlist: PlaylistEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlaylistEntity.handle(_connection, playlist)
  }

  public override fun getAllPlaylists(): Flow<List<PlaylistEntity>> {
    val _sql: String = "SELECT * FROM playlists ORDER BY id DESC"
    return createFlow(__db, false, arrayOf("playlists")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _cursorIndexOfSongs: Int = getColumnIndexOrThrow(_stmt, "songs")
        val _result: MutableList<PlaylistEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_cursorIndexOfName)
          val _tmpSongs: List<Song>
          val _tmp: String
          _tmp = _stmt.getText(_cursorIndexOfSongs)
          _tmpSongs = __converters.fromString(_tmp)
          _item = PlaylistEntity(_tmpId,_tmpName,_tmpSongs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlaylistById(id: Int): PlaylistEntity? {
    val _sql: String = "SELECT * FROM playlists WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _cursorIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _cursorIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _cursorIndexOfSongs: Int = getColumnIndexOrThrow(_stmt, "songs")
        val _result: PlaylistEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_cursorIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_cursorIndexOfName)
          val _tmpSongs: List<Song>
          val _tmp: String
          _tmp = _stmt.getText(_cursorIndexOfSongs)
          _tmpSongs = __converters.fromString(_tmp)
          _result = PlaylistEntity(_tmpId,_tmpName,_tmpSongs)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

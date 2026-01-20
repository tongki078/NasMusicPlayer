package com.nas.musicplayer.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
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
public class RecentSearchDao_Impl(
  __db: RoomDatabase,
) : RecentSearchDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRecentSearch: EntityInsertAdapter<RecentSearch>
  init {
    this.__db = __db
    this.__insertAdapterOfRecentSearch = object : EntityInsertAdapter<RecentSearch>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `recent_searches` (`query`,`timestamp`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RecentSearch) {
        statement.bindText(1, entity.query)
        statement.bindLong(2, entity.timestamp)
      }
    }
  }

  public override suspend fun insertSearch(recentSearch: RecentSearch): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRecentSearch.insert(_connection, recentSearch)
  }

  public override fun getRecentSearches(): Flow<List<RecentSearch>> {
    val _sql: String = "SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10"
    return createFlow(__db, false, arrayOf("recent_searches")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _cursorIndexOfQuery: Int = getColumnIndexOrThrow(_stmt, "query")
        val _cursorIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<RecentSearch> = mutableListOf()
        while (_stmt.step()) {
          val _item: RecentSearch
          val _tmpQuery: String
          _tmpQuery = _stmt.getText(_cursorIndexOfQuery)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_cursorIndexOfTimestamp)
          _item = RecentSearch(_tmpQuery,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSearch(query: String) {
    val _sql: String = "DELETE FROM recent_searches WHERE `query` = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM recent_searches"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

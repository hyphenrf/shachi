package com.hyphenrf.shachi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hyphenrf.shachi.data.model.*

@Database(entities = [
    Server::class,
    SelectedServer::class,
    Post::class,
    SavedSearch::class,
    BlacklistedTag::class,
    ServerBlacklistedTagCrossRef::class,
    PostTag::class, Tag::class,
    SearchHistory::class],
    views = [ServerView::class],
    version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun blacklistedTagDao(): BlacklistedTagDao
    abstract fun tagDao(): TagDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        private var db: AppDatabase? = null
        fun build(context: Context): AppDatabase = db ?: synchronized(this) {
            Room.databaseBuilder(context, AppDatabase::class.java, "shachi")
                .addMigrations(MIG_1to2)
                .build()
                .also { db = it }
        }
    }
}

private val MIG_1to2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) =
        database.execSQL("ALTER TABLE favorite ADD COLUMN title TEXT")
}
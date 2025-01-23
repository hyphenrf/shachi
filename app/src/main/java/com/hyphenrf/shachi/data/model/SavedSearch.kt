package com.hyphenrf.shachi.data.model

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "saved_search",
    indices = [
        Index("server_id"),
        Index("tags", "saved_search_title", "server_id", unique = true)],
    foreignKeys = [ForeignKey(childColumns = ["server_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        parentColumns = ["server_id"],
        entity = Server::class)])
data class SavedSearch(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "saved_search_id") val savedSearchId: Int = 0,
    val tags: String,
    @ColumnInfo(name = "saved_search_title") val savedSearchTitle: String,
    @ColumnInfo(name = "server_id") val serverId: Int,
    @ColumnInfo(name = "saved_search_order") val order: Int,
    @ColumnInfo(name = "date_added",
        defaultValue = "CURRENT_TIMESTAMP") val dateAdded: Long,
)

data class SavedSearchServer(
    @Embedded val savedSearch: SavedSearch,
    @Relation(
        parentColumn = "server_id",
        entityColumn = "server_id"
    )
    val server: ServerView,
)
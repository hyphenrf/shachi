package com.hyphenrf.shachi.data.model

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "search_history",
    indices = [
        Index(value = ["tags", "server_id"], unique = true),
        Index("server_id"),
        Index("created_at")],
    foreignKeys = [ForeignKey(childColumns = ["server_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        parentColumns = ["server_id"],
        entity = Server::class)])
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val searchHistoryId: Int = 0,
    @ColumnInfo(name = "server_id") val serverId: Int,
    val tags: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

data class SearchHistoryServer(
    @Embedded val searchHistory: SearchHistory,
    @Relation(
        parentColumn = "server_id",
        entityColumn = "server_id"
    )
    val server: ServerView,
)
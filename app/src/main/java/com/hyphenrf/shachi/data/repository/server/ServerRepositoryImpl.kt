package com.hyphenrf.shachi.data.repository.server

import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.repository.ServerRepository

class ServerRepositoryImpl(override val db: AppDatabase) : ServerRepository
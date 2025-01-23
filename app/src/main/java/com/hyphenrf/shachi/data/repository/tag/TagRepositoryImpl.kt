package com.hyphenrf.shachi.data.repository.tag

import com.hyphenrf.shachi.data.database.AppDatabase
import com.hyphenrf.shachi.data.api.BooruApi

class TagRepositoryImpl(override val booruApi: BooruApi, override val db: AppDatabase) :
    TagRepository
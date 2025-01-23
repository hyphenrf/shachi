package com.hyphenrf.shachi.data.util

fun String.isManualSearchTags() = this.contains(Regex("[{}~]"))
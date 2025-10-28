package com.example.firstaidapp.data.models

data class SearchHistory(
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resultCount: Int = 0,
    val category: String? = null
)

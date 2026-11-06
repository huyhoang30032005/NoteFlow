package com.example.noteflow.model

data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val category: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
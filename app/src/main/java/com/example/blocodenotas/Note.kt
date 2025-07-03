package com.example.blocodenotas

data class Note(
    val id: Long,
    var title: String,
    var content: String,
    var lastModified: Long
)
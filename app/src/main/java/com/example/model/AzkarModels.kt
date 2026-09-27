package com.example.model

data class AzkarItem(
    val id: Int,
    val text: String,
    val count: Int = 1,
    val note: String? = null
)

data class AzkarCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val items: List<AzkarItem>
)

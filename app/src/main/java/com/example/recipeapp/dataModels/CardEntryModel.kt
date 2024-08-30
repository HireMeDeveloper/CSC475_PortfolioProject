package com.example.recipeapp.dataModels

class CardEntryModel(
    val title: String,
    val description: String,
    val text: String,
    val type: String, // breakfast, lunch, dinner, dessert, hint, trend
    val icon: Int,
    val height: Int,
    val isRecipe: Boolean,
    var isFavorite: Boolean,
    val isHealthy: Boolean,
    val size: String // light, medium, heavy
) {
    var databaseID: Int? = null
}
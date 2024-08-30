package com.example.recipeapp.dataModels

import android.icu.util.Calendar
import java.sql.Date

class DailyHabitModel(
    val date: Date,
    var breakfastAmount: Int,
    var lunchAmount: Int,
    var dinnerAmount: Int,
    var dessertAmount: Int,
    var workoutAmount: Int
) {
    var databaseID: Int? = null
}


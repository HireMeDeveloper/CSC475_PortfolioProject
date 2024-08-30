package com.example.recipeapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.recipeapp.dataModels.CardEntryModel
import com.example.recipeapp.dataModels.DailyHabitModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.util.Calendar
import java.util.Date

class RecommendationManager() {

    companion object {
        private var instance: RecommendationManager? = null

        fun getInstance(context: Context?): RecommendationManager? {
            return if (context != null) {
                instance ?: synchronized(this) {
                    instance ?: RecommendationManager().also { instance = it }
                }
            } else {
                null
            }
        }
    }

    fun findTrends(baseData: DailyHabitModel, userData : ArrayList<DailyHabitModel>) : Array<Boolean>{
        var trendArray = arrayOf(
            false, false,
            false, false,
            false, false,
            false, false,
            false, false
        )

        var countReduction = 0

        // Read the data to find which trends are active, using an array of booleans for the trend ids
        var breakfastTotal = 0
        var lunchTotal = 0
        var dinnerTotal = 0
        var dessertTotal = 0
        var workoutTotal = 0

        for (entry in userData){
            if (entry.databaseID === baseData.databaseID){
                countReduction = 1
                continue
            }

            breakfastTotal += entry.breakfastAmount
            lunchTotal += entry.lunchAmount
            dinnerTotal += entry.dinnerAmount
            dessertTotal += entry.dessertAmount
            workoutTotal += entry.workoutAmount
        }

        val count = userData.size - countReduction
        val breakfastAverage = breakfastTotal / count
        val lunchAverage = lunchTotal / count
        val dinnerAverage = dinnerTotal / count
        val dessertAverage = dessertTotal / count
        val workoutAverage = workoutTotal / count

        trendArray[0] = breakfastAverage < baseData.breakfastAmount
        trendArray[1] = breakfastAverage > baseData.breakfastAmount
        trendArray[2] = lunchAverage < baseData.lunchAmount
        trendArray[3] = lunchAverage > baseData.lunchAmount
        trendArray[4] = dinnerAverage < baseData.dinnerAmount
        trendArray[5] = dinnerAverage > baseData.dinnerAmount
        trendArray[6] = dessertAverage < baseData.dessertAmount
        trendArray[7] = dessertAverage > baseData.dessertAmount
        trendArray[8] = workoutAverage < baseData.workoutAmount
        trendArray[9] = workoutAverage > baseData.workoutAmount

        return trendArray
    }

    fun findRecommendations(userData : ArrayList<DailyHabitModel>) : Array<Boolean>{
        var recommendationArray = arrayOf(
            false,
            false,
            false,
            false,
            false
        )

        // Read the data to find which trends are active, using an array of booleans for the trend ids
        var breakfastTotal = 0
        var lunchTotal = 0
        var dinnerTotal = 0
        var dessertTotal = 0
        var workoutTotal = 0

        for (entry in userData){
            breakfastTotal += entry.breakfastAmount
            lunchTotal += entry.lunchAmount
            dinnerTotal += entry.dinnerAmount
            dessertTotal += entry.dessertAmount
            workoutTotal += entry.workoutAmount
        }

        val count = userData.size
        val breakfastAverage = breakfastTotal / count
        val lunchAverage = lunchTotal / count
        val dinnerAverage = dinnerTotal / count
        val dessertAverage = dessertTotal / count
        val workoutAverage = workoutTotal / count

        recommendationArray[0] = breakfastAverage > 1
        recommendationArray[1] = lunchAverage > 1
        recommendationArray[2] = dinnerAverage > 1
        recommendationArray[3] = dessertAverage > 1
        recommendationArray[4] = workoutAverage > 2

        return recommendationArray
    }
}

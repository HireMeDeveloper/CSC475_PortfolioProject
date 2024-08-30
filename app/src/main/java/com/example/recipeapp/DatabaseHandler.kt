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

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {

    companion object {
        private var instance: DatabaseHandler? = null

        fun getInstance(context: Context?): DatabaseHandler? {
            return if (context != null) {
                instance ?: synchronized(this) {
                    instance ?: DatabaseHandler(context.applicationContext).also { instance = it }
                }
            } else {
                null
            }
        }

        const val VERSION = 1
        const val ID = "id"

        const val TYPE = "type"
        const val SIZE = "size"
        const val NAME = "cardEntryDatabase"
        const val CARD_TABLE = "card_table"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val TEXT = "text"
        const val ICON = "icon"
        const val HEIGHT = "height"
        const val IS_RECIPE = "is_recipe"
        const val IS_FAVORITE = "is_favorite"
        const val IS_HEALTHY = "is_healthy"

        const val CREATE_CARD_TABLE =
            "CREATE TABLE IF NOT EXISTS $CARD_TABLE(" +
                    "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TITLE TEXT, " +
                    "$DESCRIPTION TEXT, " +
                    "$TEXT TEXT," +
                    "$TYPE TEXT," +
                    "$ICON INTEGER," +
                    "$HEIGHT INTEGER," +
                    "$IS_RECIPE INTEGER," +
                    "$IS_FAVORITE INTEGER," +
                    "$IS_HEALTHY INTEGER," +
                    "$SIZE TEXT" +
                    ")"

        const val USER_TABLE = "user_table"
        const val DATE = "date"
        const val BREAKFAST_AMOUNT = "breakfast_amount"
        const val LUNCH_AMOUNT = "lunch_amount"
        const val DINNER_AMOUNT = "dinner_amount"
        const val DESSERT_AMOUNT = "dessert_amount"
        const val WORKOUT_AMOUNT = "workout_amount"

        const val CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS $USER_TABLE(" +
                    "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$DATE TEXT," +
                    "$BREAKFAST_AMOUNT INTEGER," +
                    "$LUNCH_AMOUNT INTEGER," +
                    "$DINNER_AMOUNT INTEGER," +
                    "$DESSERT_AMOUNT INTEGER," +
                    "$WORKOUT_AMOUNT INTEGER" +
                    ")"
    }

    private var recommendationManager : RecommendationManager? = null

    private var db: SQLiteDatabase = this.writableDatabase

    init {
        loadDatabase(context.applicationContext)

        recommendationManager = RecommendationManager.getInstance(context)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_CARD_TABLE)
        db?.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $CARD_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE")
        onCreate(db)
    }

    fun insertTask(task: CardEntryModel) {
        val cv = ContentValues().apply {
            put(TITLE, task.title)
            put(DESCRIPTION, task.description)
            put(TEXT, task.text)
            put(TYPE, task.type)
            put(ICON, task.icon)
            put(HEIGHT, task.height)
            put(IS_RECIPE, if (task.isRecipe) 1 else 0)
            put(IS_FAVORITE, if (task.isFavorite) 1 else 0)
            put(IS_HEALTHY, if (task.isHealthy) 1 else 0)
            put(SIZE, task.size)
        }
        db.insert(CARD_TABLE, null, cv)
    }

    @SuppressLint("Range")
    fun getCards(table: String, columns: Array<String>?, whereClause: String?, whereArgs: Array<String>?, orderBy: String?): List<CardEntryModel> {
        val cardEntryList: MutableList<CardEntryModel> = ArrayList()
        var cur: Cursor? = null

        try {
            db.beginTransaction()
            cur = db.query(table, columns, whereClause, whereArgs, null, null, orderBy)
            if (cur != null && cur.moveToFirst()) {
                do {
                    val title = cur.getString(cur.getColumnIndex(TITLE))
                    val description = cur.getString(cur.getColumnIndex(DESCRIPTION))
                    val text = cur.getString(cur.getColumnIndex(TEXT))
                    val type = cur.getString(cur.getColumnIndex(TYPE))
                    val icon = cur.getInt(cur.getColumnIndex(ICON))
                    val height = cur.getInt(cur.getColumnIndex(HEIGHT))
                    val isRecipe = cur.getInt(cur.getColumnIndex(IS_RECIPE)) == 1
                    val isFavorite = cur.getInt(cur.getColumnIndex(IS_FAVORITE)) == 1
                    val isHealthy = cur.getInt(cur.getColumnIndex(IS_HEALTHY)) == 1
                    val size = cur.getString(cur.getColumnIndex(SIZE))
                    val cardEntry = CardEntryModel(
                        title, description, text, type,
                        icon, height,
                        isRecipe, isFavorite,
                        isHealthy, size
                    )
                    cardEntry.databaseID = cur.getInt(cur.getColumnIndex(ID))
                    cardEntryList.add(cardEntry)
                } while (cur.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("com.example.recipeapp.DatabaseHandler", "Error while trying to get tasks from database", e)
        } finally {
            db.endTransaction()
            cur?.close()
        }
        return cardEntryList
    }

    fun getAllCards(): List<CardEntryModel> {
        return getCards(CARD_TABLE, null,null, null, null)
    }

    fun getAllRecipes(): List<CardEntryModel> {
        return getCards(CARD_TABLE, null, "is_recipe = ?", arrayOf("1"), null)
    }

    fun getRecommendedRecipes() : List<CardEntryModel>? {
        val allHabits = getAllHabitEntries()
        if (allHabits.isEmpty()) return null

        var recommendedRecipes = mutableSetOf<CardEntryModel>()
        val recommendations = recommendationManager?.findRecommendations(
            allHabits as ArrayList<DailyHabitModel>
        )

        val isHealthier = recommendations?.get(4) == true
        val healthyString = if (isHealthier) "1" else "0"

        val query = "$TYPE = ? AND $IS_FAVORITE = ? AND $IS_HEALTHY = ?"
        println("Generated Query: $query")

        val breakfasts = getCards(CARD_TABLE, null, "$TYPE = ? AND $IS_FAVORITE = ?", arrayOf("breakfast", "0"), null).shuffled()
        val lunches = getCards(CARD_TABLE, null, "$TYPE = ? AND $IS_FAVORITE = ?", arrayOf("lunch", "0"), null).shuffled()
        val dinners = getCards(CARD_TABLE, null, "$TYPE = ? AND $IS_FAVORITE = ?", arrayOf("dinner", "0"), null).shuffled()
        val desserts = getCards(CARD_TABLE, null, "$TYPE = ? AND $IS_FAVORITE = ?", arrayOf("dessert", "0"), null).shuffled()

        if (recommendations?.get(0) == true) {
            recommendedRecipes.add(breakfasts.random())
            recommendedRecipes.add(breakfasts.random())
        }

        if (recommendations?.get(1) == true) {
            recommendedRecipes.add(lunches.random())
            recommendedRecipes.add(lunches.random())
        }

        if (recommendations?.get(2) == true) {
            recommendedRecipes.add(dinners.random())
            recommendedRecipes.add(dinners.random())
        }

        if (recommendations?.get(3) == true) {
            recommendedRecipes.add(desserts.random())
            recommendedRecipes.add(desserts.random())
        }

        val allrecipes = getCards(CARD_TABLE, null, "$IS_FAVORITE = ?", arrayOf("0"), null).shuffled()
        recommendedRecipes.add(allrecipes.random())
        recommendedRecipes.add(allrecipes.random())

        return recommendedRecipes.toList()

    }

    fun getAllFavoriteRecipes(): List<CardEntryModel> {
        return getCards(CARD_TABLE, null, "is_favorite = ?", arrayOf("1"), null)
    }

    fun getAllHints(): List<CardEntryModel> {
        return getCards(CARD_TABLE, null, "type = ?", arrayOf("hint"), null)
    }

    fun getAllTrends(): List<CardEntryModel> {
        return getCards(CARD_TABLE, null, "type = ?", arrayOf("trend"), null)
    }

    fun getReleventTrends(): List<CardEntryModel>? {
        val baseHabit = getBaseHabitEntry()
        val allHabits = getAllHabitEntries()
        if (baseHabit == null || allHabits.isEmpty()) return null

        val trendArray = recommendationManager?.findTrends(baseHabit,
            allHabits as ArrayList<DailyHabitModel>
        )

        val allTrends = getAllTrends()
        var releventTrends = mutableListOf<CardEntryModel>()
        if (trendArray?.get(0) == true) releventTrends.add(allTrends[0])
        if (trendArray?.get(1) == true) releventTrends.add(allTrends[1])
        if (trendArray?.get(2) == true) releventTrends.add(allTrends[2])
        if (trendArray?.get(3) == true) releventTrends.add(allTrends[3])
        if (trendArray?.get(4) == true) releventTrends.add(allTrends[4])
        if (trendArray?.get(5) == true) releventTrends.add(allTrends[5])
        if (trendArray?.get(6) == true) releventTrends.add(allTrends[6])
        if (trendArray?.get(7) == true) releventTrends.add(allTrends[7])
        if (trendArray?.get(8) == true) releventTrends.add(allTrends[8])
        if (trendArray?.get(9) == true) releventTrends.add(allTrends[9])

        return releventTrends
    }

    fun searchDatabase(keyword: String, isFavorite: Boolean): List<CardEntryModel> {
        val selection = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        // Search keyword condition across multiple columns
        selection.append("""
        ($TITLE LIKE ? OR 
        $DESCRIPTION LIKE ? OR 
        $TEXT LIKE ? OR 
        $TYPE LIKE ?)
    """.trimIndent())

        val keywordPattern = "%$keyword%"
        selectionArgs.addAll(listOf(keywordPattern, keywordPattern, keywordPattern, keywordPattern))

        // Filter by isFavorite
        selection.append(" AND $IS_FAVORITE = ?")
        selectionArgs.add(if (isFavorite) "1" else "0")

        // Call getTasks with the constructed query
        return getCards(
            table = CARD_TABLE,
            columns = null,
            whereClause = selection.toString(),
            whereArgs = selectionArgs.toTypedArray(),
            orderBy = null
        )
    }

    fun updateFavorite(entry: CardEntryModel, isFavorite: Boolean) {
        val cv = ContentValues().apply {
            put(IS_FAVORITE, if (isFavorite) 1 else 0)
        }
        db.update(CARD_TABLE, cv, "$ID=?", arrayOf(entry.databaseID.toString()))
    }

    fun deleteCard(id: Int) {
        db.delete(CARD_TABLE, "$ID=?", arrayOf(id.toString()))
    }

    fun insertOrUpdateDailyHabit(dailyHabit : DailyHabitModel) {
        val existingEntry = getEntryByDate(dailyHabit.date.toString())

        if (existingEntry == null) {
            val cv = ContentValues().apply {
                put(DATE, dailyHabit.date.toString())
                put(BREAKFAST_AMOUNT, dailyHabit.breakfastAmount)
                put(LUNCH_AMOUNT, dailyHabit.lunchAmount)
                put(DINNER_AMOUNT, dailyHabit.dinnerAmount)
                put(DESSERT_AMOUNT, dailyHabit.dessertAmount)
                put(WORKOUT_AMOUNT, dailyHabit.workoutAmount)
            }
            db.insert(USER_TABLE, null, cv)

            ensureEntryLimit()
        } else {
            val cv = ContentValues().apply {
                put(BREAKFAST_AMOUNT, dailyHabit.breakfastAmount)
                put(LUNCH_AMOUNT, dailyHabit.lunchAmount)
                put(DINNER_AMOUNT, dailyHabit.dinnerAmount)
                put(DESSERT_AMOUNT, dailyHabit.dessertAmount)
                put(WORKOUT_AMOUNT, dailyHabit.workoutAmount)
            }
            db.update(USER_TABLE, cv, "$DATE = ?", arrayOf(dailyHabit.date.toString()))
        }
    }

    @SuppressLint("Range")
    fun getDailyHabitEntries(table: String, columns: Array<String>?, whereClause: String?, whereArgs: Array<String>?, orderBy: String?): List<DailyHabitModel> {
        db?.execSQL(CREATE_USER_TABLE)

        val dailyHabitList: MutableList<DailyHabitModel> = ArrayList()
        var cur: Cursor? = null

        try {
            db.beginTransaction()
            cur = db.query(USER_TABLE, columns, whereClause, whereArgs, null, null, orderBy)
            if (cur != null && cur.moveToFirst()) {
                do {
                    val dateString = cur.getString(cur.getColumnIndex(DATE))
                    val breakfastAmount = cur.getInt(cur.getColumnIndex(BREAKFAST_AMOUNT))
                    val lunchAmount = cur.getInt(cur.getColumnIndex(LUNCH_AMOUNT))
                    val dinnerAmount = cur.getInt(cur.getColumnIndex(DINNER_AMOUNT))
                    val dessertAmount = cur.getInt(cur.getColumnIndex(DESSERT_AMOUNT))
                    val workoutAmount = cur.getInt(cur.getColumnIndex(WORKOUT_AMOUNT))
                    val dailyHabitModel = DailyHabitModel(
                        java.sql.Date.valueOf(dateString), breakfastAmount, lunchAmount, dinnerAmount, dessertAmount, workoutAmount
                    )
                    dailyHabitModel.databaseID = cur.getInt(cur.getColumnIndex(ID))
                    dailyHabitList.add(dailyHabitModel)
                } while (cur.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("com.example.recipeapp.DatabaseHandler", "Error while trying to get tasks from database", e)
        } finally {
            db.endTransaction()
            cur?.close()
        }
        return dailyHabitList
    }

    fun getAllHabitEntries(): List<DailyHabitModel>{
        return getDailyHabitEntries(USER_TABLE, null, null, null, null)
    }

    fun submitOrUpdateDailyValue(id: Int, amount: Int){
        val todaysDate = java.sql.Date(Calendar.getInstance().getTime().getTime())

        var newEntry = DailyHabitModel(
            todaysDate, 0, 0, 0, 0, 0
        )

        val todaysEntry = getDailyHabitModelByDate(todaysDate.toString())

        if (todaysEntry != null) {
            newEntry = todaysEntry
        }

        when (id) {
            0 -> newEntry.breakfastAmount = amount
            1 -> newEntry.lunchAmount = amount
            2 -> newEntry.dinnerAmount = amount
            3 -> newEntry.dessertAmount = amount
            4 -> newEntry.workoutAmount = amount
        }

        insertOrUpdateDailyHabit(newEntry)
    }

    @SuppressLint("Range")
    private fun ensureEntryLimit() {
        // Query to get all entries except the entry with ID 0, ordered by date (or ID)
        val query = "SELECT $ID FROM $USER_TABLE WHERE $DATE != (SELECT MIN($DATE) FROM $USER_TABLE) ORDER BY $DATE DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.count > 10) {
            // We need to delete older entries to maintain the limit
            val excessCount = cursor.count - 10
            var deletedCount = 0

            cursor.use {
                while (it.moveToNext() && deletedCount < excessCount) {
                    val id = it.getInt(it.getColumnIndex(ID))
                    db.delete(USER_TABLE, "$ID = ?", arrayOf(id.toString()))
                    deletedCount++
                }
            }
        }
        cursor.close()
    }

    private fun getEntryByDate(date: String): Cursor? {
        val query = "SELECT * FROM $USER_TABLE WHERE $DATE = ? AND $ID != 1"
        val cursor = db.rawQuery(query, arrayOf(date))
        if (cursor.moveToFirst()) {
            return cursor
        } else {
            cursor.close()
            return null
        }
    }

    @SuppressLint("Range")
    private fun getDailyHabitModelByDate(date: String) : DailyHabitModel?{
        val cur = getEntryByDate(date) ?: return null

        val dateString = cur.getString(cur.getColumnIndex(DATE))
        val breakfastAmount = cur.getInt(cur.getColumnIndex(BREAKFAST_AMOUNT))
        val lunchAmount = cur.getInt(cur.getColumnIndex(LUNCH_AMOUNT))
        val dinnerAmount = cur.getInt(cur.getColumnIndex(DINNER_AMOUNT))
        val dessertAmount = cur.getInt(cur.getColumnIndex(DESSERT_AMOUNT))
        val workoutAmount = cur.getInt(cur.getColumnIndex(WORKOUT_AMOUNT))
        val dailyHabitModel = DailyHabitModel(
            java.sql.Date.valueOf(dateString), breakfastAmount, lunchAmount, dinnerAmount, dessertAmount, workoutAmount
        )
        dailyHabitModel.databaseID = cur.getInt(cur.getColumnIndex(ID))

        return dailyHabitModel
    }

    fun getBaseHabitEntry(): DailyHabitModel?{
        val entries = getAllHabitEntries()
        return if (entries.isNotEmpty()) entries[0] else null
    }

    fun updateBaseHabitEntry(dailyHabit: DailyHabitModel) {
        val firstEntry = getBaseHabitEntry()

        if (firstEntry == null) {
            val cv = ContentValues().apply {
                put(DATE, dailyHabit.date.toString())
                put(BREAKFAST_AMOUNT, dailyHabit.breakfastAmount)
                put(LUNCH_AMOUNT, dailyHabit.lunchAmount)
                put(DINNER_AMOUNT, dailyHabit.dinnerAmount)
                put(DESSERT_AMOUNT, dailyHabit.dessertAmount)
                put(WORKOUT_AMOUNT, dailyHabit.workoutAmount)
            }
            db.insert(USER_TABLE, null, cv)

            ensureEntryLimit()
        } else {
            val cv = ContentValues().apply {
                put(BREAKFAST_AMOUNT, dailyHabit.breakfastAmount)
                put(LUNCH_AMOUNT, dailyHabit.lunchAmount)
                put(DINNER_AMOUNT, dailyHabit.dinnerAmount)
                put(DESSERT_AMOUNT, dailyHabit.dessertAmount)
                put(WORKOUT_AMOUNT, dailyHabit.workoutAmount)
            }
            db.update(USER_TABLE, cv, "$DATE = ?", arrayOf(dailyHabit.date.toString()))
        }
    }

    fun removeDailyHabit(id: Int){
        db.delete(USER_TABLE, "ID=?", arrayOf(id.toString()))
    }

    fun loadDatabase(context: Context){
        if (getAllCards().isEmpty()){
            // Add all the card entry models
            val json1 = readAsset(context, "database.json")
            val json2 = readAsset(context, "hints.json")
            val json3 = readAsset(context, "trends.json")
            if (json1 === null || json2 === null || json3 === null) {
                Log.d("check", "JSON was null")
                return
            }

            val jsonObject1 = JSONObject(json1)
            val jsonObject2 = JSONObject(json2)
            val jsonObject3 = JSONObject(json3)

            val jsonArray1 = jsonObject1.getJSONArray("entries")
            val jsonArray2 = jsonObject2.getJSONArray("entries")
            val jsonArray3 = jsonObject3.getJSONArray("entries")

            val combinedJsonArray = JSONArray()

            for (i in 0 until jsonArray1.length()) {
                combinedJsonArray.put(jsonArray1.get(i))
            }

            for (i in 0 until jsonArray2.length()) {
                combinedJsonArray.put(jsonArray2.get(i))
            }

            for (i in 0 until jsonArray3.length()) {
                combinedJsonArray.put(jsonArray3.get(i))
            }

            for (i in 0 until combinedJsonArray.length()) {
                val entry = combinedJsonArray.getJSONObject(i)

                val title = entry.getString("title")
                val description = entry.getString("description")
                val text = entry.getString("text")
                val type = entry.getString("type")
                val icon = entry.getInt("icon")
                val height = entry.getDouble("height").toFloat()
                val isRecipe = entry.getBoolean("isRecipe")
                val isFavorite = entry.getBoolean("isFavorite")
                val isHealthy = entry.getBoolean("isHealthy")
                val size = entry.getString("size")

                // Add to the list
                insertTask(CardEntryModel(
                    title,
                    description,
                    text,
                    type,
                    icon,
                    height.toInt(),
                    isRecipe,
                    isFavorite,
                    isHealthy,
                    size
                ))
            }
        }
    }
    
    

    private fun readAsset(context: Context?, fileName: String) : String? {
        return context?.assets?.open(fileName)?.bufferedReader()?.use(BufferedReader::readText)
    }
}

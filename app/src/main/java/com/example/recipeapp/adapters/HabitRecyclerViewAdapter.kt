package com.example.recipeapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.DatabaseHandler
import com.example.recipeapp.R
import com.example.recipeapp.dataModels.DailyHabitModel
import com.example.recipeapp.dataModels.HabitModel
import java.util.Calendar

class HabitRecyclerViewAdapter(private val context: Context?, private val habitCards : ArrayList<HabitModel>) : RecyclerView.Adapter<HabitRecyclerViewAdapter.HabitViewHolder>() {

    private var dbHandler : DatabaseHandler? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.card_habit, parent, false)

        return HabitViewHolder(view)
    }

    override fun getItemCount(): Int {
        return habitCards.size
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        dbHandler = DatabaseHandler.getInstance(context)

        val settings = arrayListOf(
            "None",
            "Light",
            "Medium",
            "Heavy"
        )

        holder.titleTextView.text = habitCards[position].title
        holder.currentTextView.text = settings[habitCards[position].setting]
        holder.seekBar.progress = habitCards[position].setting

        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentCard = habitCards[holder.adapterPosition]

                currentCard.setting = progress
                holder.currentTextView.text = settings[habitCards[holder.adapterPosition].setting]

                // TODO: Update selection in the database
                var currentEntry = dbHandler?.getBaseHabitEntry()
                if (currentEntry == null) {
                    currentEntry = DailyHabitModel(
                        java.sql.Date(Calendar.getInstance().getTime().getTime()),
                        0, 0, 0, 0, 0
                    )
                }

                when (holder.adapterPosition) {
                    0 -> currentEntry.breakfastAmount = progress
                    1 -> currentEntry.lunchAmount = progress
                    2 -> currentEntry.dinnerAmount = progress
                    3 -> currentEntry.dessertAmount = progress
                    4 -> currentEntry.workoutAmount = progress
                }

                dbHandler?.updateBaseHabitEntry(currentEntry)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Handle the start of touch interaction
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Handle the end of touch interaction
            }
        })
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView : TextView = itemView.findViewById(R.id.habitTitleTextView)
        var currentTextView: TextView = itemView.findViewById(R.id.habitCurrentTextView)
        var seekBar: SeekBar = itemView.findViewById(R.id.habitSeekBar)
    }
}
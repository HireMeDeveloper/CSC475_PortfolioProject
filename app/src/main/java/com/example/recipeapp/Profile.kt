package com.example.recipeapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.adapters.HabitRecyclerViewAdapter
import com.example.recipeapp.adapters.HintRecyclerViewAdapter
import com.example.recipeapp.dataModels.CardEntryModel
import com.example.recipeapp.dataModels.DailyHabitModel
import com.example.recipeapp.dataModels.HabitModel
import com.example.recipeapp.dataModels.HintModel
import java.sql.Date
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

var habitEntryModels : ArrayList<HabitModel> = ArrayList()
private var dbHandler : DatabaseHandler? = null

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        dbHandler = DatabaseHandler.getInstance(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = setUpHabitModels(inflater, container)

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setUpHabitModels(inflater: LayoutInflater, container: ViewGroup?) : View?{
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val recyclerView : RecyclerView? = view?.findViewById(R.id.profileRecyclerView)

        // TODO: Populate with stored values
        var habitSettingArray = arrayOf(0, 0, 0, 0, 0)

        val baseHabitEntry = dbHandler?.getBaseHabitEntry()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val theDayBefore = Date(calendar.time.time)

        if (baseHabitEntry == null) {
            dbHandler?.updateBaseHabitEntry(DailyHabitModel(
                theDayBefore,
                0, 0, 0, 0, 0
            ))
        } else {
            habitSettingArray = arrayOf(
                baseHabitEntry.breakfastAmount,
                baseHabitEntry.lunchAmount,
                baseHabitEntry.dinnerAmount,
                baseHabitEntry.dessertAmount,
                baseHabitEntry.workoutAmount
            )
        }

        habitEntryModels.add(HabitModel(
            "Ideal Daily Breakfast Size",
            habitSettingArray[0]
        ))
        habitEntryModels.add(HabitModel(
            "Ideal Daily Lunch Size",
            habitSettingArray[1]
        ))
        habitEntryModels.add(HabitModel(
            "Ideal Daily Dinner Size",
            habitSettingArray[2]
        ))
        habitEntryModels.add(HabitModel(
            "Ideal Daily Dessert Size",
            habitSettingArray[3]
        ))
        habitEntryModels.add(HabitModel(
            "Ideal Daily Workout Amount",
            habitSettingArray[4]
        ))

        val habitViewAdapter = HabitRecyclerViewAdapter(context, habitEntryModels)
        recyclerView?.adapter = habitViewAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return view
    }
}
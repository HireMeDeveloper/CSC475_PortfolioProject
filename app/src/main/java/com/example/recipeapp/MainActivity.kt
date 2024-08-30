package com.example.recipeapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.recipeapp.adapters.FragmentPageAdapter
import com.example.recipeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FragmentPageAdapter
    private lateinit var binding : ActivityMainBinding

    private var dbHandler : DatabaseHandler? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load database singleton for the first time
        dbHandler = DatabaseHandler.getInstance(this)

        adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val viewPager2 = binding.viewPager2
        viewPager2.adapter = adapter

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val activity = binding.bottomNavigationView.findViewById<View>(R.id.activity)
        activity.isEnabled = false

        activity.setOnTouchListener { _, event -> run {
            if (event.action == MotionEvent.ACTION_UP) {
                showBottomSheet()
            }
        }
            true
        }

        viewPager2.currentItem = 0

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> viewPager2.currentItem = 0
                R.id.trends -> viewPager2.currentItem = 1
                R.id.meals -> viewPager2.currentItem = 2
                R.id.profile -> viewPager2.currentItem = 3

                else -> {

                }
            }
            true
        }

        viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNavigationView.findViewById<View>(R.id.home).performClick()
                    1 -> binding.bottomNavigationView.findViewById<View>(R.id.trends).performClick()
                    2 -> binding.bottomNavigationView.findViewById<View>(R.id.meals).performClick()
                    3 -> binding.bottomNavigationView.findViewById<View>(R.id.profile).performClick()
                    else -> {

                    }
                }
            }
        })

    }

    private fun showBottomSheet() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet)

        val breakfastLayout = dialog.findViewById<LinearLayout>(R.id.layoutBreakfast)
        val lunchLayout = dialog.findViewById<LinearLayout>(R.id.layoutLunch)
        val dinnerLayout = dialog.findViewById<LinearLayout>(R.id.layoutDinner)
        val workoutLayout = dialog.findViewById<LinearLayout>(R.id.layoutWorkout)
        val dessertLayout = dialog.findViewById<LinearLayout>(R.id.layoutDessert)

        breakfastLayout.setOnClickListener {
            showQuantitySelectionSheet(dialog, "breakfast")
        }

        lunchLayout.setOnClickListener {
            showQuantitySelectionSheet(dialog, "lunch")
        }

        dinnerLayout.setOnClickListener {
            showQuantitySelectionSheet(dialog, "dinner")
        }

        workoutLayout.setOnClickListener {
            showQuantitySelectionSheet(dialog, "workout")
        }

        dessertLayout.setOnClickListener {
            showQuantitySelectionSheet(dialog, "dessert")
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun showQuantitySelectionSheet(dialog: Dialog, source: String) {
        dialog.setContentView(R.layout.bottom_sheet_quantity)

        val noneLayout = dialog.findViewById<LinearLayout>(R.id.layoutNone)
        val lightLayout = dialog.findViewById<LinearLayout>(R.id.layoutLight)
        val mediumLayout = dialog.findViewById<LinearLayout>(R.id.layoutMedium)
        val heavyLayout = dialog.findViewById<LinearLayout>(R.id.layoutHeavy)

        val replacementDrawable = when (source) {
            "breakfast" -> R.drawable.breakfast_icon
            "lunch" -> R.drawable.lunch_icon
            "dinner" -> R.drawable.dinner_icon
            "workout" -> R.drawable.workout_icon
            else -> {
                R.drawable.desert_icon
            }
        }

        val habitId = when(source) {
            "breakfast" -> 0
            "lunch" -> 1
            "dinner" -> 2
            "dessert" -> 3
            else -> {
                4
            }
        }

        dialog.findViewById<ImageView>(R.id.image_none)?.setImageResource(replacementDrawable)
        dialog.findViewById<ImageView>(R.id.image_light)?.setImageResource(replacementDrawable)
        dialog.findViewById<ImageView>(R.id.image_medium)?.setImageResource(replacementDrawable)
        dialog.findViewById<ImageView>(R.id.image_heavy)?.setImageResource(replacementDrawable)

        noneLayout.setOnClickListener {
            Toast.makeText(baseContext, "Selected no $source.", Toast.LENGTH_SHORT).show()
            dialog.hide()

            dbHandler?.submitOrUpdateDailyValue(habitId, 0)
        }

        lightLayout.setOnClickListener {
            Toast.makeText(baseContext, "Selected a light $source", Toast.LENGTH_SHORT).show()
            dialog.hide()

            dbHandler?.submitOrUpdateDailyValue(habitId, 1)
        }

        mediumLayout.setOnClickListener {
            Toast.makeText(baseContext, "Selected a medium $source", Toast.LENGTH_SHORT).show()
            dialog.hide()

            dbHandler?.submitOrUpdateDailyValue(habitId, 2)
        }

        heavyLayout.setOnClickListener {
            Toast.makeText(baseContext, "Selected a heavy $source", Toast.LENGTH_SHORT).show()
            dialog.hide()

            dbHandler?.submitOrUpdateDailyValue(habitId, 3)
        }
    }

}
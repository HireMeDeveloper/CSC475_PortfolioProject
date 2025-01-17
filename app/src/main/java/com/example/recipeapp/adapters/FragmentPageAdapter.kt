package com.example.recipeapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.recipeapp.Home
import com.example.recipeapp.Meals
import com.example.recipeapp.Profile
import com.example.recipeapp.Trends

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                Home()
            }
            1 -> {
                Trends()
            }
            2 -> {
                Meals()
            }
            else -> {
                Profile()
            }
        }
    }
}
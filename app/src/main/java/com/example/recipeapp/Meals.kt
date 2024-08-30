package com.example.recipeapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.widget.SearchView.OnCloseListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.adapters.CardEntryRecyclerViewAdapter
import com.example.recipeapp.dataModels.CardEntryModel
import com.google.android.material.search.SearchBar


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var dbHandler : DatabaseHandler? = null

var cardEntryModels : ArrayList<CardEntryModel> = ArrayList()
var isFavoriteFilterOn = false
/**
 * A simple [Fragment] subclass.
 * Use the [Meals.newInstance] factory method to
 * create an instance of this fragment.
 */
class Meals : Fragment() {
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
        val view = setUpMealModels(inflater, container)
        val favoriteToggle = view?.findViewById<ImageView>(R.id.mealsFavoriteToggleImageView)

        favoriteToggle?.setOnClickListener {
            if (isFavoriteFilterOn) {
                isFavoriteFilterOn = false
                favoriteToggle.setImageResource(R.drawable.open_heart_icon)
            } else {
                isFavoriteFilterOn = true
                favoriteToggle.setImageResource(R.drawable.closed_heart_icon)
            }

            updateFavoriteFilter(view)
        }

        val searchView = view?.findViewById<SearchView>(R.id.searchView)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()){
                    hideKeyboard()
                    updateFavoriteFilter(view)
                    return true
                }

                // Optionally perform search as the user types
                newText?.let {
                    cardEntryModels.clear()
                    cardEntryModels.addAll(dbHandler!!.searchDatabase(it, isFavoriteFilterOn) as ArrayList<CardEntryModel>)

                    val recyclerView : RecyclerView? = view.findViewById(R.id.mealsRecyclerView)
                    recyclerView?.adapter?.notifyDataSetChanged()
                }
                return true
            }
        })

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
         * @return A new instance of fragment Meals.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Meals().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun hideKeyboard(){
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun setUpMealModels(inflater: LayoutInflater, container: ViewGroup?) : View?{
        val view = inflater.inflate(R.layout.fragment_meals, container, false)
        val recyclerView : RecyclerView? = view?.findViewById(R.id.mealsRecyclerView)

        // Get only meals from database
        if (dbHandler != null){
            cardEntryModels = dbHandler!!.getAllRecipes() as ArrayList<CardEntryModel>
        } else {
            return view
        }

        val cardEntryAdapter = CardEntryRecyclerViewAdapter(context, cardEntryModels)
        recyclerView?.adapter = cardEntryAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateFavoriteFilter(view: View){
        if (dbHandler != null){
            if (isFavoriteFilterOn) {
                //cardEntryModels = dbHandler!!.getAllFavoriteRecipes() as ArrayList<CardEntryModel>
                cardEntryModels.clear()
                cardEntryModels.addAll(dbHandler!!.getAllFavoriteRecipes() as ArrayList<CardEntryModel>)
            } else {
                cardEntryModels.clear()
                cardEntryModels.addAll(dbHandler!!.getAllRecipes() as ArrayList<CardEntryModel>)
            }
            Log.i("Confirm", "update function ran")

            val recyclerView : RecyclerView? = view.findViewById(R.id.mealsRecyclerView)
            recyclerView?.adapter?.notifyDataSetChanged()
        }
    }
}
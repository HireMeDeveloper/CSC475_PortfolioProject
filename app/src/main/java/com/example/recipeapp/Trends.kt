package com.example.recipeapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.adapters.CardEntryRecyclerViewAdapter
import com.example.recipeapp.dataModels.CardEntryModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [Trends.newInstance] factory method to
 * create an instance of this fragment.
 */
class Trends : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var dbHandler : DatabaseHandler? = null
    var cardEntryModels : ArrayList<CardEntryModel> = ArrayList()

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

        val view = setUpTrendModels(inflater, container)
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
         * @return A new instance of fragment Trends.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Trends().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setUpTrendModels(inflater: LayoutInflater, container: ViewGroup?) : View?{
        val view = inflater.inflate(R.layout.fragment_trends, container, false)
        val recyclerView : RecyclerView? = view?.findViewById(R.id.trendsRecyclerView)

        // Get only meals from database
        if (dbHandler != null){
            cardEntryModels = dbHandler!!.getReleventTrends() as ArrayList<CardEntryModel>
        } else {
            return view
        }

        val cardEntryAdapter = CardEntryRecyclerViewAdapter(context, cardEntryModels)
        recyclerView?.adapter = cardEntryAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return view
    }
}
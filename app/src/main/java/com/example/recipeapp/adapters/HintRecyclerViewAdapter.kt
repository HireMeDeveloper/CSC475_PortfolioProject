package com.example.recipeapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.R
import com.example.recipeapp.dataModels.HintModel

class HintRecyclerViewAdapter(private val context: Context?, private val hintModels: ArrayList<HintModel>) : RecyclerView.Adapter<HintRecyclerViewAdapter.HintViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.card_hint, parent, false)

        return HintViewHolder(view)
    }

    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        holder.titleTextView.text = hintModels[position].title
        holder.bodyTextView.text = hintModels[position].body
    }

    override fun getItemCount(): Int {
        return hintModels.size
    }

    class HintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView : TextView = itemView.findViewById(R.id.titleTextView)
        var bodyTextView: TextView = itemView.findViewById(R.id.bodyTextView)
    }
}
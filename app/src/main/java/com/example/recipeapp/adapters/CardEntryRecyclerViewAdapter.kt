package com.example.recipeapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeapp.DatabaseHandler
import com.example.recipeapp.R
import com.example.recipeapp.dataModels.CardEntryModel

class CardEntryRecyclerViewAdapter(private val context: Context?, private val cardEntryModels: ArrayList<CardEntryModel>) : RecyclerView.Adapter<CardEntryRecyclerViewAdapter.CardEntryViewHolder>(){

    private val icons = arrayListOf(
        R.drawable.breakfast_icon,
        R.drawable.lunch_icon,
        R.drawable.dinner_icon,
        R.drawable.desert_icon,
        R.drawable.idea_icon,
        R.drawable.graph_icon,
        R.drawable.meals_icon
    )

    private var dbHandler: DatabaseHandler? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardEntryViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.card_universal, parent, false)

        return CardEntryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cardEntryModels.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: CardEntryViewHolder, position: Int) {
        val currentCard = cardEntryModels[position]

        holder.titleTextView.text = if (currentCard.isHealthy) "Healthy " + currentCard.title else currentCard.title
        holder.bodyTextView.text = currentCard.description
        holder.recpieTextView.text = currentCard.text
        holder.iconImageView.setImageResource(icons[currentCard.icon])

        val params = holder.iconImageView.layoutParams
        val heightInDp = currentCard.height
        val density = context?.resources?.displayMetrics?.density
        if (density != null) {
            val heightInPx = (heightInDp * density).toInt()
            params.height = heightInPx
        }

        holder.iconImageView.layoutParams = params

        holder.expandTextView.visibility = if (currentCard.isRecipe) View.VISIBLE else View.GONE
        holder.favoriteImageView.visibility = if (currentCard.isRecipe) View.VISIBLE else View.INVISIBLE

        if (currentCard.isRecipe) {
            holder.favoriteImageView.setImageResource(if (currentCard.isFavorite) R.drawable.closed_heart_icon else R.drawable.open_heart_icon)
        }

        dbHandler = DatabaseHandler.getInstance(context)

        holder.favoriteImageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                currentCard.isFavorite = !currentCard.isFavorite
                holder.favoriteImageView.setImageResource(if (currentCard.isFavorite) R.drawable.closed_heart_icon else R.drawable.open_heart_icon)

                dbHandler?.updateFavorite(currentCard, currentCard.isFavorite)

                true
            } else {
                false
            }
        }

        holder.expandTextView.text = "Click To Expand"
        holder.recpieTextView.visibility = View.GONE
        holder.isExpanded = false

        holder.view.setOnClickListener {
            if (!currentCard.isRecipe) return@setOnClickListener
            if (holder.isExpanded) {
                holder.expandTextView.text = "Click To Expand"
                holder.recpieTextView.visibility = View.GONE
                holder.isExpanded = false
            } else {
                holder.expandTextView.text = "Click To Collapse"
                holder.recpieTextView.visibility = View.VISIBLE
                holder.isExpanded = true
            }
        }
    }

    class CardEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view : View = itemView
        var titleTextView : TextView = itemView.findViewById(R.id.universalTitleTextView)
        var bodyTextView: TextView = itemView.findViewById(R.id.universalBodyTextView)
        var expandTextView: TextView = itemView.findViewById(R.id.expandTextView)
        var iconImageView: ImageView = itemView.findViewById(R.id.universalIconImageView)
        var favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)
        var recpieTextView : TextView = itemView.findViewById(R.id.universalRecipeTextView)
        var isExpanded : Boolean = false
    }
}
package com.example.horairebusmihanbot.vue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DirectionsAdapter(
    private var directions: MutableList<String>
) : RecyclerView.Adapter<DirectionsAdapter.DirectionViewHolder>() {

    /**
     * Widget de la vue pour chaque élément de la liste
     */
    class DirectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }
    /**
     * Nombre d'éléments dans la liste
     */
    override fun getItemCount(): Int = directions.size

    /**
     * Création de la vue pour chaque élément de la liste
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_1, // Utilisation d'un layout simple
            parent,
            false
        )
        return DirectionViewHolder(view)
    }

    /**
     * Remplissage de la vue avec les données
     */
    override fun onBindViewHolder(holder: DirectionViewHolder, position: Int) {
        val direction = directions[position]
        holder.textView.text = direction
    }

    /**
     * Met à jour la liste des directions
     */
    fun updateDirections(newDirections: List<String>) {
        directions.clear()
        directions.addAll(newDirections)
        notifyDataSetChanged()
    }
}
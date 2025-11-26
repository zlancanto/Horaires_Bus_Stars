package com.example.horairebusmihanbot.vue.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.horairebusmihanbot.data.entity.BusRoute
import androidx.core.graphics.toColorInt

/**
 * Adaptateur personnalisé pour le Spinner des lignes de bus.
 * Permet de styliser chaque élément avec la couleur de fond et la couleur de texte
 * définies dans la base de données (BusRoute.routeColor et BusRoute.routeTextColor).
 */
class BusRouteAdapter(
    private val context: Context,
    private var routes: MutableList<BusRoute>
) : BaseAdapter() {

    override fun getCount(): Int = routes.size

    override fun getItem(position: Int): BusRoute = routes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * Méthode pour mettre à jour
     * la liste des lignes de bus via le LiveData du ViewModel
     */
    fun updateRoutes(newRoutes: List<BusRoute>) {
        routes.clear()
        routes.addAll(newRoutes)
        notifyDataSetChanged()
    }

    /**
     * Crée la vue de l'élément sélectionné affiché dans le Spinner
     * (quand il est fermé).
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromRoute(position, convertView, parent, isDropdown = false)
    }

    /**
     * Crée la vue de l'élément lorsqu'il est
     * dans le menu déroulant du Spinner.
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromRoute(position, convertView, parent, isDropdown = true)
    }

    /**
     * Logique de création de vue partagée.
     */
    private fun createViewFromRoute(
        position: Int,
        convertView: View?,
        parent: ViewGroup?,
        isDropdown: Boolean
    ): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_dropdown_item, // Layout de base Android pour un TextView simple
            parent,
            false
        )

        val route = routes[position]
        val textView = view.findViewById<TextView>(android.R.id.text1)

        // Nom court de la ligne (ex: "C1")
        textView.text = route.shortName

        // Application des couleurs
        try {
            /*
             * Conversion des couleurs hexadécimales
             * (stockées dans la base) en objets Color Android
             */
            val backgroundColor = "#${route.color}".toColorInt()
            textView.setBackgroundColor(backgroundColor)
            val textColor = "#${route.textColor}".toColorInt()
            textView.setTextColor(textColor)

        } catch (_: IllegalArgumentException) {
            // Si les chaînes de couleur dans la DB ne sont pas valides
            textView.setBackgroundColor(Color.GRAY)
            textView.setTextColor(Color.BLACK)
        }

        // Ajouter un padding pour une meilleure esthétique
        val padding = if (isDropdown) 16 else 8
        val paddingPx = (padding * context.resources.displayMetrics.density).toInt()
        view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

        return view
    }
}
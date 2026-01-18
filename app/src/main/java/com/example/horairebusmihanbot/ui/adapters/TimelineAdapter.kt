package com.example.horairebusmihanbot.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.horairebusmihanbot.R
import com.example.horairebusmihanbot.dto.StopTimeWithLabelDto

class TimelineAdapter : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    private var stops: List<StopTimeWithLabelDto> = emptyList()

    fun submitList(newList: List<StopTimeWithLabelDto>) {
        stops = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_stop, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val stop = stops[position]
        holder.bind(stop, position == 0, position == stops.size - 1)
    }

    override fun getItemCount() = stops.size

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.text_time)
        private val name: TextView = itemView.findViewById(R.id.text_stop_name)
        private val line: View = itemView.findViewById(R.id.timeline_line)

        fun bind(stop: StopTimeWithLabelDto, isFirst: Boolean, isLast: Boolean) {
            time.text = stop.departure_time
            name.text = stop.stop_name

            // Logique de design : cacher les bouts de ligne inutiles
            line.visibility = View.VISIBLE
        }
    }
}
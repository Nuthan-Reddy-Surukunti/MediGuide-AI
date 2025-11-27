package com.mediguide.firstaid.ui.guides

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediguide.firstaid.R
import com.mediguide.firstaid.data.models.FirstAidGuide

/**
 * Grid adapter for compact guide cards - optimized for 2-column layout
 */
class GuideGridAdapter(
    private val onGuideClick: (FirstAidGuide) -> Unit
) : ListAdapter<FirstAidGuide, GuideGridAdapter.GuideViewHolder>(GuideDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guide_card_grid, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGuideIcon: TextView = itemView.findViewById(R.id.tvGuideIcon)
        private val tvGuideTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val viewSeverityIndicator: View = itemView.findViewById(R.id.viewSeverityIndicator)

        fun bind(guide: FirstAidGuide) {
            tvGuideTitle.text = guide.title
            tvCategory.text = guide.category

            // Set icon based on category
            tvGuideIcon.text = getGuideEmoji(guide)

            // Set severity indicator color
            val severityColor = when (guide.category.lowercase()) {
                "critical" -> R.color.severity_critical
                "serious" -> R.color.severity_high
                "common" -> R.color.severity_low
                else -> R.color.severity_medium
            }
            viewSeverityIndicator.setBackgroundColor(
                ContextCompat.getColor(itemView.context, severityColor)
            )

            itemView.setOnClickListener {
                onGuideClick(guide)
            }
        }

        private fun getGuideEmoji(guide: FirstAidGuide): String {
            return when {
                guide.id.contains("cpr", ignoreCase = true) -> "❤️"
                guide.id.contains("bleeding", ignoreCase = true) -> "🩸"
                guide.id.contains("burn", ignoreCase = true) -> "🔥"
                guide.id.contains("choking", ignoreCase = true) -> "🫁"
                guide.id.contains("fracture", ignoreCase = true) ||
                guide.id.contains("bone", ignoreCase = true) -> "🦴"
                guide.id.contains("shock", ignoreCase = true) -> "⚡"
                guide.id.contains("stroke", ignoreCase = true) -> "🧠"
                guide.id.contains("heart", ignoreCase = true) -> "💔"
                guide.id.contains("seizure", ignoreCase = true) -> "⚠️"
                guide.id.contains("poison", ignoreCase = true) -> "☠️"
                guide.id.contains("bite", ignoreCase = true) ||
                guide.id.contains("sting", ignoreCase = true) -> "🐝"
                guide.id.contains("head", ignoreCase = true) -> "🤕"
                guide.id.contains("eye", ignoreCase = true) -> "👁️"
                guide.id.contains("allergic", ignoreCase = true) -> "🤧"
                guide.id.contains("asthma", ignoreCase = true) -> "💨"
                guide.id.contains("heat", ignoreCase = true) -> "🌡️"
                guide.id.contains("cold", ignoreCase = true) ||
                guide.id.contains("hypothermia", ignoreCase = true) -> "❄️"
                guide.id.contains("sprain", ignoreCase = true) -> "🦵"
                guide.id.contains("wound", ignoreCase = true) -> "🩹"
                else -> "🏥"
            }
        }
    }

    class GuideDiffCallback : DiffUtil.ItemCallback<FirstAidGuide>() {
        override fun areItemsTheSame(oldItem: FirstAidGuide, newItem: FirstAidGuide): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FirstAidGuide, newItem: FirstAidGuide): Boolean {
            return oldItem == newItem
        }
    }
}


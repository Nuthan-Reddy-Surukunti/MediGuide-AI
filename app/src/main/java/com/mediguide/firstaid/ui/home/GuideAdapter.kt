package com.mediguide.firstaid.ui.home

// Adapter for displaying guide cards on the home screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediguide.firstaid.data.models.FirstAidGuide
import com.mediguide.firstaid.databinding.ItemGuideCardBinding
import androidx.core.content.ContextCompat
import com.mediguide.firstaid.R

class GuideAdapter(
    private val onGuideClick: (FirstAidGuide) -> Unit,
    private val onViewDemoClick: (String) -> Unit
) : ListAdapter<FirstAidGuide, GuideAdapter.GuideViewHolder>(GuideDiffCallback()) {

    // Inflate item layout and create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val binding = ItemGuideCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GuideViewHolder(binding)
    }

    // Bind the guide data to the view holder
    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GuideViewHolder(
        private val binding: ItemGuideCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Populate UI with guide data and hook up click listeners
        fun bind(guide: FirstAidGuide) {
            binding.tvGuideTitle.text = guide.title
            binding.tvGuideDescription.text = guide.description
            binding.tvCategory.text = guide.category

            // Set guide icon based on category - using fallback icon
            val iconResource = R.drawable.ic_medical_default
            binding.ivGuideImage.setImageResource(iconResource)

            // Set severity indicator with color coding
            binding.tvSeverity.text = guide.severity
            when (guide.severity) {
                "CRITICAL" -> binding.tvSeverity.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.severity_critical)
                )
                "HIGH" -> binding.tvSeverity.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.severity_high)
                )
                "MEDIUM" -> binding.tvSeverity.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.severity_medium)
                )
                else -> binding.tvSeverity.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.severity_low)
                )
            }

            // Handle guide card click -> navigate or open detail
            binding.root.setOnClickListener {
                onGuideClick(guide)
            }

            // View Demo click -> open video if available
            binding.tvViewDemo.setOnClickListener {
                if (guide.youtubeLink?.isNotEmpty() == true) {
                    onViewDemoClick(guide.youtubeLink ?: "")
                }
            }

            // Show or hide demo button based on availability
            binding.tvViewDemo.visibility = if (guide.youtubeLink?.isNotEmpty() == true) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    // Diff callback for efficient list updates
    class GuideDiffCallback : DiffUtil.ItemCallback<FirstAidGuide>() {
        override fun areItemsTheSame(oldItem: FirstAidGuide, newItem: FirstAidGuide): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FirstAidGuide, newItem: FirstAidGuide): Boolean {
            return oldItem == newItem
        }
    }
}

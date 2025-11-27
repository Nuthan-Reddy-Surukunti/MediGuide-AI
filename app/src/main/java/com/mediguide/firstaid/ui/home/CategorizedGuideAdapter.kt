package com.mediguide.firstaid.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mediguide.firstaid.R
import com.mediguide.firstaid.data.models.FirstAidGuide
import com.mediguide.firstaid.databinding.ItemGuideCardBinding
import androidx.core.content.ContextCompat

class CategorizedGuideAdapter(
    private val onGuideClick: (FirstAidGuide) -> Unit,
    private val onCategoryClick: (String) -> Unit,
    private val onViewDemoClick: (String) -> Unit
) : ListAdapter<CategoryItem, RecyclerView.ViewHolder>(CategoryItemDiffCallback()) {

    companion object {
        // View type for category header
        private const val VIEW_TYPE_CATEGORY_HEADER = 0
        // View type for a guide item
        private const val VIEW_TYPE_GUIDE_ITEM = 1
    }

    // Determine view type based on the CategoryItem sealed class
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CategoryItem.CategoryHeader -> VIEW_TYPE_CATEGORY_HEADER
            is CategoryItem.GuideItem -> VIEW_TYPE_GUIDE_ITEM
        }
    }

    // Inflate the appropriate view holder for each view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            VIEW_TYPE_GUIDE_ITEM -> {
                val binding = ItemGuideCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GuideItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    // Bind data to either header or guide view holder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryItem.CategoryHeader -> {
                (holder as CategoryHeaderViewHolder).bind(item)
            }
            is CategoryItem.GuideItem -> {
                (holder as GuideItemViewHolder).bind(item.guide)
            }
        }
    }

    inner class CategoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cache header views
        private val tvCategoryTitle: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        private val tvCategoryDescription: TextView = itemView.findViewById(R.id.tvCategoryDescription)
        private val tvCategoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        private val tvGuideCount: TextView = itemView.findViewById(R.id.tvGuideCount)
        private val tvExpandIcon: TextView = itemView.findViewById(R.id.tvExpandIcon)

        // Bind header data and hook category click to toggle
        fun bind(categoryHeader: CategoryItem.CategoryHeader) {
            tvCategoryTitle.text = categoryHeader.title
            tvCategoryDescription.text = categoryHeader.description
            tvCategoryIcon.text = categoryHeader.icon
            tvGuideCount.text = "${categoryHeader.guideCount} guides"

            // Show caret based on expanded state
            tvExpandIcon.text = if (categoryHeader.isExpanded) "\u25bc" else "\u25b6"

            itemView.setOnClickListener {
                onCategoryClick(categoryHeader.title)
            }
        }
    }

    inner class GuideItemViewHolder(
        private val binding: ItemGuideCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind guide item view with data and listeners
        fun bind(guide: FirstAidGuide) {
            binding.tvGuideTitle.text = guide.title
            binding.tvGuideDescription.text = guide.description
            binding.tvCategory.text = guide.category
            binding.tvSeverity.text = guide.severity

            // Use fallback icon for guides
            val iconResource = R.drawable.ic_medical_default
            binding.ivGuideImage.setImageResource(iconResource)

            // Map severity to color resource
            val severityColor = when (guide.severity.uppercase()) {
                "CRITICAL" -> R.color.severity_critical
                "HIGH" -> R.color.severity_high
                "MODERATE" -> R.color.severity_medium
                else -> R.color.severity_low
            }
            binding.tvSeverity.setBackgroundResource(severityColor)

            // Click to open guide detail
            binding.root.setOnClickListener {
                onGuideClick(guide)
            }

            // View demo video if link present
            binding.tvViewDemo.setOnClickListener {
                if (guide.youtubeLink?.isNotEmpty() == true) {
                    onViewDemoClick(guide.youtubeLink ?: "")
                }
            }

            // Show or hide demo button
            binding.tvViewDemo.visibility = if (guide.youtubeLink?.isNotEmpty() == true) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    // Diff callback for efficient list updates
    class CategoryItemDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return when {
                oldItem is CategoryItem.CategoryHeader && newItem is CategoryItem.CategoryHeader -> {
                    oldItem.title == newItem.title
                }
                oldItem is CategoryItem.GuideItem && newItem is CategoryItem.GuideItem -> {
                    oldItem.guide.id == newItem.guide.id
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

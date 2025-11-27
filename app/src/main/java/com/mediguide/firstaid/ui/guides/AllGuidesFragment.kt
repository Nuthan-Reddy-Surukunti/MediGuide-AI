package com.mediguide.firstaid.ui.guides

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mediguide.firstaid.R
import com.mediguide.firstaid.data.models.FirstAidGuide
import com.mediguide.firstaid.databinding.FragmentAllGuidesBinding
import com.mediguide.firstaid.managers.JsonGuideManager
import com.mediguide.firstaid.ui.home.HomeViewModel

/**
 * AllGuidesFragment - Displays all available first aid guides in a grid layout
 */
class AllGuidesFragment : Fragment() {

    private var _binding: FragmentAllGuidesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var gridAdapter: GuideGridAdapter
    private lateinit var guideManager: JsonGuideManager

    private var allGuides: List<FirstAidGuide> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllGuidesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        guideManager = JsonGuideManager(requireContext())

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupSearchFunctionality()
        setupEmergencyActions()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private fun setupRecyclerView() {
        gridAdapter = GuideGridAdapter { guide ->
            // Navigate to guide detail
            try {
                val action = AllGuidesFragmentDirections.actionAllGuidesToGuideDetail(guide.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.rvGuides.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gridAdapter
            itemAnimator = null
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun setupObservers() {
        // Observe all guides
        guideManager.getAllGuides().observe(viewLifecycleOwner) { guides ->
            allGuides = guides
            updateStatistics(guides)
            filterGuides(binding.etSearch.text.toString().trim())
        }
    }

    private fun updateStatistics(guides: List<FirstAidGuide>) {
        val critical = guides.count { it.category.equals("Critical", ignoreCase = true) }
        val serious = guides.count { it.category.equals("Serious", ignoreCase = true) }
        val common = guides.count { it.category.equals("Common", ignoreCase = true) }

        // Update statistics header
        view?.findViewById<TextView>(R.id.tvTotalGuides)?.text = "${guides.size} Guides"
        view?.findViewById<TextView>(R.id.tvCriticalCount)?.text = critical.toString()
        view?.findViewById<TextView>(R.id.tvSeriousCount)?.text = serious.toString()
        view?.findViewById<TextView>(R.id.tvCommonCount)?.text = common.toString()
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                // Show/hide headers based on search state
                if (query.isNotEmpty()) {
                    // Hide headers when searching
                    view?.findViewById<View>(R.id.layoutStatistics)?.visibility = View.GONE
                    view?.findViewById<View>(R.id.layoutEmergencyActions)?.visibility = View.GONE
                } else {
                    // Show headers when not searching
                    view?.findViewById<View>(R.id.layoutStatistics)?.visibility = View.VISIBLE
                    view?.findViewById<View>(R.id.layoutEmergencyActions)?.visibility = View.VISIBLE
                }

                filterGuides(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun setupEmergencyActions() {
        // Call 112 button
        view?.findViewById<View>(R.id.btnCall911)?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:112")
                }
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Unable to open dialer", Toast.LENGTH_SHORT).show()
            }
        }

        // CPR Quick Access
        view?.findViewById<View>(R.id.btnQuickCPR)?.setOnClickListener {
            navigateToGuide("cpr")
        }

        // Bleeding Quick Access
        view?.findViewById<View>(R.id.btnQuickBleeding)?.setOnClickListener {
            navigateToGuide("severe_bleeding")
        }

        // Choking Quick Access
        view?.findViewById<View>(R.id.btnQuickChoking)?.setOnClickListener {
            navigateToGuide("choking")
        }
    }

    private fun navigateToGuide(guideId: String) {
        try {
            val action = AllGuidesFragmentDirections.actionAllGuidesToGuideDetail(guideId)
            findNavController().navigate(action)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Guide not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterGuides(searchQuery: String) {
        var filteredGuides = allGuides

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filteredGuides = filteredGuides.filter { guide ->
                guide.title.contains(searchQuery, ignoreCase = true) ||
                guide.description.contains(searchQuery, ignoreCase = true) ||
                guide.category.contains(searchQuery, ignoreCase = true)
            }
        }

        gridAdapter.submitList(filteredGuides)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


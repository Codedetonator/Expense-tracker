package com.sujal.expensetracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sujal.expensetracker.MainActivity
import com.sujal.expensetracker.R
import com.sujal.expensetracker.databinding.FragmentHomeBinding
import com.sujal.expensetracker.databinding.ItemBarRowBinding
import com.sujal.expensetracker.databinding.ItemExpenseBinding
import com.sujal.expensetracker.ui.trips.TripsFragment
import com.sujal.expensetracker.ui.wishlist.WishlistFragment
import com.sujal.expensetracker.util.emojiForCategory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val moneyFormat = DecimalFormat("₹#,##0.##")
    private val headerDateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    private val rowDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textDate.text = headerDateFormat.format(Date())
        binding.textSeeAll.setOnClickListener {
            (activity as? MainActivity)?.selectTab(MainActivity.TAB_EXPENSES)
        }
        binding.tileWishlist.setOnClickListener {
            (activity as? MainActivity)?.openSubScreen(WishlistFragment(), "wishlist")
        }
        binding.tileTrips.setOnClickListener {
            (activity as? MainActivity)?.openSubScreen(TripsFragment(), "trips")
        }

        viewModel.stats.observe(viewLifecycleOwner) { render(it) }
        viewModel.totalMoney.observe(viewLifecycleOwner) {
            binding.textTotalMoney.text = moneyFormat.format(it)
        }
    }

    private fun render(stats: HomeStats) {
        binding.textMonthTotal.text = moneyFormat.format(stats.monthTotal)
        binding.textMonthHint.text = when {
            stats.lastMonthTotal > 0 -> "Last month ${moneyFormat.format(stats.lastMonthTotal)}"
            stats.monthTotal == 0.0 -> "Tap + to log your first expense"
            else -> ""
        }
        binding.textMonthHint.visibility =
            if (binding.textMonthHint.text.isEmpty()) View.GONE else View.VISIBLE
        binding.textToday.text = moneyFormat.format(stats.todayTotal)

        binding.chartWeek.setData(stats.weekLabels, stats.weekValues)
        renderCategories(stats.categoryTotals)
        renderRecent(stats)
    }

    private fun renderCategories(totals: List<Pair<String, Double>>) {
        val container = binding.containerCategoryBars
        container.removeAllViews()

        if (totals.isEmpty()) {
            container.addView(emptyMessage(getString(R.string.no_expenses_yet)))
            return
        }

        val top = totals.take(5)
        val max = top.maxOf { it.second }
        top.forEach { (category, amount) ->
            val row = ItemBarRowBinding.inflate(LayoutInflater.from(requireContext()), container, false)
            row.textBarLabel.text = "${emojiForCategory(category)} $category"
            row.textBarAmount.text = moneyFormat.format(amount)
            row.progressBar.max = 1000
            row.progressBar.progress = ((amount / max) * 1000).toInt()
            container.addView(row.root)
        }
    }

    private fun renderRecent(stats: HomeStats) {
        val container = binding.containerRecent
        container.removeAllViews()

        if (stats.recent.isEmpty()) {
            container.addView(emptyMessage(getString(R.string.no_expenses_yet)))
            binding.textSeeAll.visibility = View.GONE
            return
        }
        binding.textSeeAll.visibility = View.VISIBLE

        stats.recent.forEach { item ->
            val row = ItemExpenseBinding.inflate(LayoutInflater.from(requireContext()), container, false)
            row.textIcon.text = emojiForCategory(item.category)
            row.textDescription.text = item.description
            row.textSubtitle.text = "${item.category} · ${rowDateFormat.format(Date(item.dateMillis))}"
            row.textAmount.text = moneyFormat.format(item.amount)
            row.btnDelete.visibility = View.GONE
            container.addView(row.root)
        }
    }

    private fun emptyMessage(text: String) = TextView(requireContext()).apply {
        this.text = text
        setTextColor(requireContext().getColor(R.color.text_secondary))
        textSize = 13f
        setPadding(0, 12, 0, 12)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

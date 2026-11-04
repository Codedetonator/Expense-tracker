package com.sujal.expensetracker.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujal.expensetracker.R
import com.sujal.expensetracker.data.entity.ExpenseEntity
import com.sujal.expensetracker.databinding.FragmentExpensesBinding
import com.sujal.expensetracker.ui.expenses.adapter.ExpenseAdapter
import com.sujal.expensetracker.ui.expenses.adapter.buildExpenseListRows
import com.sujal.expensetracker.util.EXPENSE_CATEGORIES
import java.text.DecimalFormat

/** The full expense history. Adding happens from the center + button; charts live on Home. */
class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpensesViewModel by viewModels()
    private lateinit var adapter: ExpenseAdapter

    private val moneyFormat = DecimalFormat("₹#,##0.##")

    private var selectedCategoryFilter: String? = null // null = All
    private var latestExpenses: List<ExpenseEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCategoryChips()

        adapter = ExpenseAdapter(onDelete = { viewModel.delete(it) })
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter

        viewModel.allExpenses.observe(viewLifecycleOwner) { list ->
            latestExpenses = list
            applyFilterAndRender()
        }
        viewModel.totalThisMonth.observe(viewLifecycleOwner) {
            binding.textSubtitle.text = "${moneyFormat.format(it)} this month"
        }
    }

    // ---------- Category chips ----------

    private fun setUpCategoryChips() {
        binding.chipContainer.removeAllViews()
        addChip(getString(R.string.category_all), null)
        EXPENSE_CATEGORIES.forEach { (name, emoji) -> addChip("$emoji $name", name) }
    }

    private fun addChip(label: String, categoryValue: String?) {
        val selected = categoryValue == selectedCategoryFilter
        val chip = TextView(requireContext()).apply {
            text = label
            setPadding(14.dp, 8.dp, 14.dp, 8.dp)
            textSize = 13f
            setTextColor(
                requireContext().getColor(if (selected) android.R.color.white else R.color.text_primary)
            )
            setBackgroundResource(
                if (selected) R.drawable.chip_selected_bg else R.drawable.chip_unselected_bg
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 8.dp }
            setOnClickListener {
                selectedCategoryFilter = categoryValue
                setUpCategoryChips()
                applyFilterAndRender()
            }
        }
        binding.chipContainer.addView(chip)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun applyFilterAndRender() {
        val filtered = selectedCategoryFilter
            ?.let { cat -> latestExpenses.filter { it.category == cat } }
            ?: latestExpenses
        adapter.submitList(buildExpenseListRows(filtered))
        binding.textEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

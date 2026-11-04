package com.sujal.expensetracker.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sujal.expensetracker.R
import com.sujal.expensetracker.data.entity.PaymentMethod
import com.sujal.expensetracker.databinding.SheetAddExpenseBinding
import com.sujal.expensetracker.ui.expenses.ExpensesViewModel
import com.sujal.expensetracker.util.EXPENSE_CATEGORIES
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** The quick "new expense" sheet opened by the center + button. */
class AddExpenseBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetAddExpenseBinding? = null
    private val binding get() = _binding!!

    // Activity-scoped so the save isn't cancelled when this sheet closes.
    private val viewModel: ExpensesViewModel by activityViewModels()

    private val moneyFormat = DecimalFormat("₹#,##0.##")
    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    private var selectedCategory: String = EXPENSE_CATEGORIES.first().first
    private var selectedDateMillis: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        renderCategoryChips()
        renderDate()
        binding.textDate.setOnClickListener { showDatePicker() }
        binding.toggleMethod.check(R.id.btn_method_cash)
        binding.btnSave.setOnClickListener { save() }
        binding.editAmount.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun save() {
        val amount = binding.editAmount.text.toString().toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(requireContext(), "Enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        // Description is optional: fall back to the category name so quick logging stays quick.
        val description = binding.editDescription.text.toString().trim().ifBlank { selectedCategory }
        val note = binding.editNote.text.toString().trim()
        val method = if (binding.toggleMethod.checkedButtonId == R.id.btn_method_online)
            PaymentMethod.ONLINE else PaymentMethod.CASH

        viewModel.add(description, amount, selectedCategory, note, method, selectedDateMillis)
        Toast.makeText(
            requireContext(), "Added ${moneyFormat.format(amount)}", Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    // ---------- Category chips ----------

    private fun renderCategoryChips() {
        binding.chipContainer.removeAllViews()
        EXPENSE_CATEGORIES.forEach { (name, emoji) ->
            val selected = name == selectedCategory
            val chip = TextView(requireContext()).apply {
                text = "$emoji $name"
                textSize = 13f
                setPadding(14.dp, 8.dp, 14.dp, 8.dp)
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
                    selectedCategory = name
                    renderCategoryChips()
                }
            }
            binding.chipContainer.addView(chip)
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    // ---------- Date ----------

    private fun renderDate() {
        binding.textDate.text = if (DateUtils.isToday(selectedDateMillis)) {
            "📅  Today"
        } else {
            "📅  ${dateFormat.format(Date(selectedDateMillis))}"
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                cal.set(year, month, day, 12, 0, 0)
                selectedDateMillis = cal.timeInMillis
                renderDate()
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

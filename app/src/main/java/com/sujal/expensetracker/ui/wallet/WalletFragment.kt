package com.sujal.expensetracker.ui.wallet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujal.expensetracker.R
import com.sujal.expensetracker.databinding.DialogAddDenominationBinding
import com.sujal.expensetracker.databinding.DialogCountCashBinding
import com.sujal.expensetracker.databinding.FragmentWalletBinding
import com.sujal.expensetracker.util.STANDARD_DENOMINATIONS
import java.text.DecimalFormat

class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletViewModel by viewModels()
    private val moneyFormat = DecimalFormat("₹#,##0.##")

    private var suppressOnlineWatcher = false
    private var suppressCashWatcher = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.grandTotal.observe(viewLifecycleOwner) {
            binding.textGrandTotal.text = moneyFormat.format(it)
        }

        viewModel.onlineBalance.observe(viewLifecycleOwner) { amount ->
            syncFieldIfNeeded(binding.editOnlineBalance, amount) { suppressOnlineWatcher = it }
        }
        viewModel.cashBalance.observe(viewLifecycleOwner) { amount ->
            syncFieldIfNeeded(binding.editCashBalance, amount) { suppressCashWatcher = it }
        }

        binding.editOnlineBalance.addTextChangedListener(watcherFor { amount ->
            if (!suppressOnlineWatcher) viewModel.setOnlineAmount(amount)
        })
        binding.editCashBalance.addTextChangedListener(watcherFor { amount ->
            if (!suppressCashWatcher) viewModel.setCashAmount(amount)
        })

        binding.btnCountCash.setOnClickListener { showCountCashDialog() }
    }

    private fun syncFieldIfNeeded(field: android.widget.EditText, amount: Double, setSuppress: (Boolean) -> Unit) {
        val current = field.text?.toString()?.toDoubleOrNull() ?: -1.0
        if (current != amount) {
            setSuppress(true)
            field.setText(if (amount == 0.0) "" else amount.toString())
            setSuppress(false)
        }
    }

    private fun watcherFor(onChanged: (Double) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun afterTextChanged(s: Editable?) {
            onChanged(s?.toString()?.toDoubleOrNull() ?: 0.0)
        }
    }

    private fun showCountCashDialog() {
        viewModel.loadDenominationCounts { saved ->
            if (isAdded && _binding != null) openCountCashDialog(saved)
        }
    }

    private fun openCountCashDialog(saved: Map<Int, Int>) {
        val ctx = requireContext()
        // Everything typed in this dialog lives only in this draft. Nothing is saved,
        // and Cash is not touched, until the user taps "Update".
        val draft = saved.toMutableMap()
        val dialogBinding = DialogCountCashBinding.inflate(LayoutInflater.from(ctx))

        fun rows(): List<Pair<Int, Int>> =
            (STANDARD_DENOMINATIONS + draft.keys).distinct().sortedDescending()
                .map { it to (draft[it] ?: 0) }

        fun total(): Double = rows().sumOf { (value, count) -> value.toDouble() * count }

        fun refreshTotal() {
            dialogBinding.textCountedTotal.text = moneyFormat.format(total())
        }

        val adapter = DenominationAdapter { value, count ->
            draft[value] = count
            refreshTotal()
        }
        dialogBinding.recyclerCountCash.layoutManager = LinearLayoutManager(ctx)
        dialogBinding.recyclerCountCash.adapter = adapter
        adapter.submitRows(rows())
        refreshTotal()

        dialogBinding.btnAddDenomination.setOnClickListener {
            showAddDenominationDialog { value ->
                if (value !in draft) draft[value] = 0
                adapter.submitRows(rows())
            }
        }

        val dialog = AlertDialog.Builder(ctx)
            .setTitle(getString(R.string.count_my_cash))
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.update) { _, _ ->
                viewModel.updateCashFromCount(rows().toMap())
                Toast.makeText(
                    ctx, "Cash updated to ${moneyFormat.format(total())}", Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        // Without this, some keyboards resize the dialog's window oddly enough to disrupt
        // typing in the list below — same fix as the add-expense sheet.
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.show()
    }

    private fun showAddDenominationDialog(onAdded: (Int) -> Unit) {
        val dialogBinding = DialogAddDenominationBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_note_coin))
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = dialogBinding.editDenominationValue.text.toString().toIntOrNull() ?: 0
                if (value <= 0) {
                    Toast.makeText(requireContext(), "Enter a valid value", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onAdded(value)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

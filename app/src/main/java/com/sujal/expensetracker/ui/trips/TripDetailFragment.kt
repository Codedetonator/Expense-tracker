package com.sujal.expensetracker.ui.trips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujal.expensetracker.databinding.DialogAddTripExpenseBinding
import com.sujal.expensetracker.databinding.FragmentTripDetailBinding
import com.sujal.expensetracker.databinding.ItemBarRowBinding
import java.text.DecimalFormat

/** Detail screen for one trip's shared wallet: total spent, who paid what, and the expense log. */
class TripDetailFragment : Fragment() {

    private var _binding: FragmentTripDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TripDetailViewModel by viewModels()
    private lateinit var adapter: TripExpenseAdapter
    private val moneyFormat = DecimalFormat("₹#,##0.##")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripId = requireArguments().getLong(ARG_TRIP_ID)
        viewModel.init(tripId)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        adapter = TripExpenseAdapter(onDelete = { viewModel.deleteExpense(it) })
        binding.recyclerTripExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTripExpenses.adapter = adapter

        viewModel.trip.observe(viewLifecycleOwner) { trip ->
            if (trip == null) return@observe
            binding.textTripName.text = trip.name
            binding.textBudgetHint.text = if (trip.budget > 0) "Budget ${moneyFormat.format(trip.budget)}" else ""
            val closed = trip.isClosed
            binding.btnCloseTrip.visibility = if (closed) View.GONE else View.VISIBLE
            binding.textClosedBanner.visibility = if (closed) View.VISIBLE else View.GONE
            binding.fabAdd.visibility = if (closed) View.GONE else View.VISIBLE
            adapter.deletable = !closed
            adapter.notifyDataSetChanged()
        }

        viewModel.expenses.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            viewModel.refreshTotals()
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) {
            binding.textTotalSpent.text = moneyFormat.format(it)
        }

        viewModel.personTotals.observe(viewLifecycleOwner) { renderPersonTotals(it) }

        binding.btnCloseTrip.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(com.sujal.expensetracker.R.string.close_trip)
                .setMessage("This trip will become read-only. You can't undo this.")
                .setPositiveButton(com.sujal.expensetracker.R.string.close_trip) { _, _ -> viewModel.closeTrip() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.fabAdd.setOnClickListener { showAddExpenseDialog() }
    }

    private fun renderPersonTotals(totals: List<Pair<String, Double>>) {
        val container = binding.containerPersonBars
        container.removeAllViews()
        if (totals.isEmpty()) return
        val max = totals.maxOf { it.second }
        totals.forEach { (person, amount) ->
            val row = ItemBarRowBinding.inflate(LayoutInflater.from(requireContext()), container, false)
            row.textBarLabel.text = person
            row.textBarAmount.text = moneyFormat.format(amount)
            row.progressBar.max = 1000
            row.progressBar.progress = ((amount / max) * 1000).toInt()
            container.addView(row.root)
        }
    }

    private fun showAddExpenseDialog() {
        val dialogBinding = DialogAddTripExpenseBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(com.sujal.expensetracker.R.string.add_trip_expense)
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val description = dialogBinding.editDescription.text.toString().trim()
                val amount = dialogBinding.editAmount.text.toString().toDoubleOrNull() ?: 0.0
                val paidBy = dialogBinding.editPaidBy.text.toString().trim()

                if (description.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter a description", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Enter an amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (paidBy.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter who paid", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.addExpense(description, amount, paidBy)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TRIP_ID = "trip_id"
        fun newInstance(tripId: Long) = TripDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_TRIP_ID, tripId) }
        }
    }
}

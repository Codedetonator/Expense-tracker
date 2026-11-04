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
import com.sujal.expensetracker.MainActivity
import com.sujal.expensetracker.databinding.DialogAddTripBinding
import com.sujal.expensetracker.databinding.FragmentTripsBinding

class TripsFragment : Fragment() {

    private var _binding: FragmentTripsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TripsViewModel by viewModels()
    private lateinit var adapter: TripsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        adapter = TripsAdapter(onClick = { trip ->
            (activity as? MainActivity)?.openSubScreen(
                TripDetailFragment.newInstance(trip.id), "trip_detail"
            )
        })
        binding.recyclerTrips.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTrips.adapter = adapter

        viewModel.allTrips.observe(viewLifecycleOwner) { trips ->
            adapter.submitList(trips)
            binding.textEmpty.visibility = if (trips.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddTripBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(com.sujal.expensetracker.R.string.add_trip)
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editName.text.toString().trim()
                val budget = dialogBinding.editBudget.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter a trip name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.add(name, budget)
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

package com.sujal.expensetracker.ui.wishlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.sujal.expensetracker.databinding.DialogAddWishlistBinding
import com.sujal.expensetracker.databinding.FragmentWishlistBinding

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WishlistViewModel by viewModels()
    private lateinit var adapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        adapter = WishlistAdapter(onDelete = { viewModel.delete(it) })
        binding.recyclerWishlist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWishlist.adapter = adapter

        viewModel.rows.observe(viewLifecycleOwner) { rows ->
            adapter.submitList(rows)
            binding.textEmpty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddWishlistBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(binding.root.context.getString(com.sujal.expensetracker.R.string.wishlist_title))
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.editName.text.toString().trim()
                val price = dialogBinding.editPrice.text.toString().toDoubleOrNull() ?: 0.0
                val weekly = dialogBinding.editWeekly.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter a name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (price <= 0) {
                    Toast.makeText(requireContext(), "Enter a price", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.add(name, price, weekly)
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

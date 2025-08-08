package com.example.kasirberas.ui.expense

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasirberas.adapter.ExpenseAdapter
import com.example.kasirberas.data.model.ExpenseStatus
import com.example.kasirberas.databinding.FragmentExpenseBinding
import com.example.kasirberas.dialog.AddExpenseDialog

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        expenseViewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]

        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupFilterSpinner()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onStatusClick = { expense ->
                showStatusUpdateDialog(expense)
            },
            onItemLongClick = { expense ->
                showExpenseOptions(expense)
            }
        )

        binding.recyclerViewExpenses.apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        expenseViewModel.filteredExpenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)

            val isEmpty = expenses.isEmpty()
            binding.recyclerViewExpenses.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        expenseViewModel.totalExpense.observe(viewLifecycleOwner) { total ->
            binding.textTotalExpense.text = total
        }

        expenseViewModel.expenseByStatus.observe(viewLifecycleOwner) { statusMap ->
            binding.textBelumBayar.text = "Belum Bayar: ${statusMap[ExpenseStatus.BELUM_BAYAR] ?: "Rp 0"}"
            binding.textCicil.text = "Cicil: ${statusMap[ExpenseStatus.CICIL] ?: "Rp 0"}"
            binding.textLunas.text = "Lunas: ${statusMap[ExpenseStatus.LUNAS] ?: "Rp 0"}"
        }

        expenseViewModel.message.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }
    }

    private fun setupFilterSpinner() {
        val filterOptions = listOf("Semua", "Belum Bayar", "Cicil", "Lunas")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = adapter

        binding.spinnerFilter.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> expenseViewModel.filterByStatus(null)
                    1 -> expenseViewModel.filterByStatus(ExpenseStatus.BELUM_BAYAR)
                    2 -> expenseViewModel.filterByStatus(ExpenseStatus.CICIL)
                    3 -> expenseViewModel.filterByStatus(ExpenseStatus.LUNAS)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun showAddExpenseDialog() {
        val dialog = AddExpenseDialog { expense ->
            expenseViewModel.addExpense(expense)
        }
        dialog.show(childFragmentManager, "AddExpenseDialog")
    }

    private fun showStatusUpdateDialog(expense: com.example.kasirberas.data.model.Expense) {
        val statusOptions = ExpenseStatus.values().map { it.displayName }.toTypedArray()
        val currentStatusIndex = ExpenseStatus.values().indexOf(expense.status)

        AlertDialog.Builder(requireContext())
            .setTitle("Update Status: ${expense.name}")
            .setSingleChoiceItems(statusOptions, currentStatusIndex) { dialog, which ->
                val newStatus = ExpenseStatus.values()[which]
                expenseViewModel.updateExpenseStatus(expense.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showExpenseOptions(expense: com.example.kasirberas.data.model.Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle(expense.name)
            .setItems(arrayOf("Update Status", "Hapus")) { _, which ->
                when (which) {
                    0 -> showStatusUpdateDialog(expense)
                    1 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Hapus Pengeluaran")
                            .setMessage("Yakin ingin menghapus ${expense.name}?")
                            .setPositiveButton("Ya") { _, _ ->
                                expenseViewModel.deleteExpense(expense.id)
                            }
                            .setNegativeButton("Tidak", null)
                            .show()
                    }
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
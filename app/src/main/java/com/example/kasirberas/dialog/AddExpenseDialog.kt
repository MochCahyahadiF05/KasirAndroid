package com.example.kasirberas.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.kasirberas.data.model.Expense
import com.example.kasirberas.data.model.ExpenseStatus
import com.example.kasirberas.databinding.DialogAddExpenseBinding
import com.example.kasirberas.utils.DateHelper
import java.util.*

class AddExpenseDialog(
    private val onExpenseAdded: (Expense) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedDueDate: Long = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddExpenseBinding.inflate(layoutInflater)

        setupSpinner()
        setupClickListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Pengeluaran")
            .setView(binding.root)
            .setPositiveButton("Simpan") { _, _ ->
                saveExpense()
            }
            .setNegativeButton("Batal", null)
            .create()
    }

    private fun setupSpinner() {
        val statuses = ExpenseStatus.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.spinnerStatus.setAdapter(adapter) // untuk AutoCompleteTextView
    }

    private fun setupClickListeners() {
        binding.buttonSelectDueDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, 23, 59, 59)
                }
                selectedDueDate = selectedCalendar.timeInMillis
                binding.textSelectedDate.text = DateHelper.formatDate(selectedDueDate)
                binding.textSelectedDate.visibility = View.VISIBLE
            },
            year, month, day
        ).show()
    }

    private fun saveExpense() {
        val name = binding.editTextName.text.toString().trim()
        val amountText = binding.editTextAmount.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val selectedStatusName = binding.spinnerStatus.text.toString()
        val statusIndex = ExpenseStatus.values().indexOfFirst { it.displayName == selectedStatusName }

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(context, "Nama pengeluaran harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountText.isEmpty()) {
            Toast.makeText(context, "Jumlah harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (statusIndex == -1) {
            Toast.makeText(context, "Pilih status pengeluaran", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(context, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            name = name,
            amount = amount,
            description = description,
            status = ExpenseStatus.values()[statusIndex],
            dueDate = if (selectedDueDate > 0) selectedDueDate else DateHelper.getCurrentTimestamp()
        )

        onExpenseAdded(expense)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

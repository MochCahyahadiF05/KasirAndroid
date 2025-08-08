package com.example.kasirberas.ui.expense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kasirberas.data.model.Expense
import com.example.kasirberas.data.model.ExpenseStatus
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.FirestoreHelper

class ExpenseViewModel : ViewModel() {

    private val firestoreHelper = FirestoreHelper()

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val _filteredExpenses = MutableLiveData<List<Expense>>()
    val filteredExpenses: LiveData<List<Expense>> = _filteredExpenses

    private val _totalExpense = MutableLiveData<String>()
    val totalExpense: LiveData<String> = _totalExpense

    private val _expenseByStatus = MutableLiveData<Map<ExpenseStatus, String>>()
    val expenseByStatus: LiveData<Map<ExpenseStatus, String>> = _expenseByStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private var allExpenses: List<Expense> = emptyList()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        _isLoading.value = true
        firestoreHelper.getAllExpenses { expenses ->
            allExpenses = expenses.sortedByDescending { it.createdAt }
            _expenses.value = allExpenses
            _filteredExpenses.value = allExpenses
            _isLoading.value = false
            calculateStats(allExpenses)
        }
    }

    fun addExpense(expense: Expense) {
        _isLoading.value = true
        firestoreHelper.addExpense(expense) { success ->
            _isLoading.value = false
            if (success) {
                _message.value = "Pengeluaran berhasil ditambahkan"
                loadExpenses()
            } else {
                _message.value = "Gagal menambah pengeluaran"
            }
        }
    }

    fun updateExpenseStatus(expenseId: String, status: ExpenseStatus) {
        _isLoading.value = true
        firestoreHelper.updateExpenseStatus(expenseId, status) { success ->
            _isLoading.value = false
            if (success) {
                _message.value = "Status berhasil diupdate"
                loadExpenses()
            } else {
                _message.value = "Gagal update status"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        _isLoading.value = true
        firestoreHelper.deleteExpense(expenseId) { success ->
            _isLoading.value = false
            if (success) {
                _message.value = "Pengeluaran berhasil dihapus"
                loadExpenses()
            } else {
                _message.value = "Gagal menghapus pengeluaran"
            }
        }
    }

    fun filterByStatus(status: ExpenseStatus?) {
        val filtered = if (status == null) {
            allExpenses
        } else {
            allExpenses.filter { it.status == status }
        }

        _filteredExpenses.value = filtered
        calculateStats(filtered)
    }

    private fun calculateStats(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        _totalExpense.value = CalculatorHelper.formatCurrency(total)

        val statusMap = ExpenseStatus.values().associate { status ->
            val statusTotal = expenses
                .filter { it.status == status }
                .sumOf { it.amount }
            status to CalculatorHelper.formatCurrency(statusTotal)
        }

        _expenseByStatus.value = statusMap
    }
}
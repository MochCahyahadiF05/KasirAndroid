package com.example.kasirberas.ui.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.DateHelper
import com.example.kasirberas.utils.FirestoreHelper
import java.util.*

class TransactionViewModel : ViewModel() {

    private val firestoreHelper = FirestoreHelper()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    private val _dailySales = MutableLiveData<String>()
    val dailySales: LiveData<String> = _dailySales

    private val _totalTransactions = MutableLiveData<Int>()
    val totalTransactions: LiveData<Int> = _totalTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        _isLoading.value = true
        firestoreHelper.getAllTransactions { transactions ->
            allTransactions = transactions
            _transactions.value = transactions
            _isLoading.value = false
            calculateStats(transactions)
        }
    }

    fun filterByToday() {
        val today = DateHelper.getCurrentTimestamp()
        val startOfDay = DateHelper.getStartOfDay(today)
        val endOfDay = DateHelper.getEndOfDay(today)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfDay..endOfDay
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    fun filterByThisWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = DateHelper.getStartOfDay(calendar.timeInMillis)

        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = DateHelper.getEndOfDay(calendar.timeInMillis)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfWeek..endOfWeek
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    fun filterByThisMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = DateHelper.getStartOfDay(calendar.timeInMillis)

        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = DateHelper.getEndOfDay(calendar.timeInMillis)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfMonth..endOfMonth
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    fun filterByDate(date: Long) {
        val startOfDay = DateHelper.getStartOfDay(date)
        val endOfDay = DateHelper.getEndOfDay(date)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfDay..endOfDay
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    fun clearFilter() {
        _filteredTransactions.value = allTransactions
        calculateStats(allTransactions)
    }

    private fun calculateStats(transactions: List<Transaction>) {
        val totalSales = transactions.sumOf { it.totalAmount }
        _dailySales.value = CalculatorHelper.formatCurrency(totalSales)
        _totalTransactions.value = transactions.size
    }
}
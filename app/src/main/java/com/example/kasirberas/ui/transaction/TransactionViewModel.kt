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

    // Semua transaksi (data mentah dari Firestore)
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    // Transaksi yang sudah difilter
    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    // Total penjualan (string sudah diformat)
    private val _dailySales = MutableLiveData<String>()
    val dailySales: LiveData<String> = _dailySales

    // Jumlah transaksi (angka)
    private val _totalTransactions = MutableLiveData<Int>()
    val totalTransactions: LiveData<Int> = _totalTransactions

    // Status loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Cache semua transaksi untuk memudahkan filter
    private var allTransactions: List<Transaction> = emptyList()

    init {
        loadTransactions()
    }

    /**
     * Ambil semua transaksi dari Firestore secara realtime
     * dan simpan ke allTransactions + LiveData utama
     */
    fun loadTransactions() {
        _isLoading.value = true
        firestoreHelper.getAllTransactionsRealtime { transactions ->
            allTransactions = transactions
            _transactions.value = transactions
            _filteredTransactions.value = transactions // default tanpa filter
            _isLoading.value = false
            calculateStats(transactions)
        }
    }

    /**
     * Filter transaksi yang terjadi hari ini
     */
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

    /**
     * Filter transaksi yang terjadi minggu ini
     */
    fun filterByThisWeek() {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY  // minggu mulai dari Senin

        // Pindah ke Senin minggu ini
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = DateHelper.getStartOfDay(calendar.timeInMillis)

        // Pindah ke Minggu minggu ini
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = DateHelper.getEndOfDay(calendar.timeInMillis)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfWeek..endOfWeek
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    /**
     * Filter transaksi yang terjadi bulan ini
     */
    fun filterByThisMonth() {
        val calendar = Calendar.getInstance()

        // Awal bulan
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = DateHelper.getStartOfDay(calendar.timeInMillis)

        // Akhir bulan
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = DateHelper.getEndOfDay(calendar.timeInMillis)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfMonth..endOfMonth
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }


    /**
     * Filter transaksi berdasarkan tanggal spesifik
     */
    fun filterByDate(date: Long) {
        val startOfDay = DateHelper.getStartOfDay(date)
        val endOfDay = DateHelper.getEndOfDay(date)

        val filtered = allTransactions.filter { transaction ->
            transaction.transactionDate in startOfDay..endOfDay
        }

        _filteredTransactions.value = filtered
        calculateStats(filtered)
    }

    /**
     * Hapus filter, tampilkan semua transaksi lagi
     */
    fun clearFilter() {
        _filteredTransactions.value = allTransactions
        calculateStats(allTransactions)
    }

    /**
     * Hitung total penjualan dan jumlah transaksi dari list yang diberikan
     */
    private fun calculateStats(transactions: List<Transaction>) {
        val totalSales = transactions.sumOf { it.totalAmount }
        _dailySales.value = CalculatorHelper.formatCurrency(totalSales)
        _totalTransactions.value = transactions.size
    }
}

package com.example.kasirberas.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasirberas.adapter.TransactionAdapter
import com.example.kasirberas.databinding.FragmentTransactionBinding
import com.example.kasirberas.utils.DateHelper
import java.util.*

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupDateFilters()
    }

    /**
     * Siapkan RecyclerView untuk menampilkan transaksi
     */
    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            onItemClick = { transaction ->
                // TODO: nanti ganti dengan pindah ke halaman detail transaksi
            }
        )

        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Observe data dari ViewModel
     * filteredTransactions dipakai supaya list ikut terfilter
     */
    private fun setupObservers() {
        transactionViewModel.filteredTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionAdapter.submitList(transactions)

            val isEmpty = transactions.isEmpty()
            binding.recyclerViewTransactions.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }

        transactionViewModel.dailySales.observe(viewLifecycleOwner) { sales ->
            binding.textDailySales.text = sales
        }

        transactionViewModel.totalTransactions.observe(viewLifecycleOwner) { total ->
            binding.textTotalTransactions.text = "Total: $total transaksi"
        }

        transactionViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * Atur event klik untuk filter dan tombol lainnya
     */
    private fun setupClickListeners() {
        binding.fabFilter.setOnClickListener {
            showDateFilterDialog()
        }

        binding.chipToday.setOnClickListener {
            transactionViewModel.filterByToday()
        }

        binding.chipThisWeek.setOnClickListener {
            transactionViewModel.filterByThisWeek()
        }

        binding.chipThisMonth.setOnClickListener {
            transactionViewModel.filterByThisMonth()
        }

        binding.chipAll.setOnClickListener {
            transactionViewModel.clearFilter()
        }
    }

    /**
     * Set filter default ke "Hari Ini"
     */
    private fun setupDateFilters() {
        binding.chipToday.isChecked = true
        transactionViewModel.filterByToday()
    }

    /**
     * Tampilkan dialog pilih tanggal custom
     */
    private fun showDateFilterDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.timeInMillis

                transactionViewModel.filterByDate(selectedDate)

                val dateText = DateHelper.formatDate(selectedDate)
                binding.chipCustom.text = dateText
                binding.chipCustom.visibility = View.VISIBLE
                binding.chipCustom.isChecked = true
            },
            year, month, day
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.kasirberas.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.kasirberas.R
import com.example.kasirberas.data.model.PaymentMethod
import com.example.kasirberas.databinding.DialogPaymentBinding
import com.example.kasirberas.utils.CalculatorHelper

class PaymentDialog(
    private val totalAmount: Double,
    private val onPaymentConfirmed: (PaymentMethod, Double) -> Unit
) : DialogFragment() {

    private var _binding: DialogPaymentBinding? = null
    private val binding get() = _binding!!

    private var selectedPaymentMethod = PaymentMethod.CASH

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPaymentBinding.inflate(layoutInflater)

        setupViews()
        setupClickListeners()
        setupTextWatchers()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pembayaran")
            .setView(binding.root)
            .setPositiveButton("Bayar") { _, _ ->
                processPayment()
            }
            .setNegativeButton("Batal", null)
            .create()
    }

    private fun setupViews() {
        binding.textTotalAmount.text = CalculatorHelper.formatCurrency(totalAmount)
        binding.editTextPaidAmount.setText(totalAmount.toString())
        calculateChange()

        // Default selection
        binding.radioButtonCash.isChecked = true
        updatePaymentMethodUI()
    }

    private fun setupClickListeners() {
        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.radioButtonCash -> PaymentMethod.CASH
                R.id.radioButtonTransfer -> PaymentMethod.TRANSFER
                else -> PaymentMethod.CASH
            }
            updatePaymentMethodUI()
        }

        binding.buttonExactAmount.setOnClickListener {
            binding.editTextPaidAmount.setText(totalAmount.toString())
        }
    }

    private fun setupTextWatchers() {
        binding.editTextPaidAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateChange()
            }
        })
    }

    private fun calculateChange() {
        val paidAmountText = binding.editTextPaidAmount.text.toString()
        val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0

        val change = CalculatorHelper.calculateChange(totalAmount, paidAmount)
        val isValid = CalculatorHelper.isValidPayment(totalAmount, paidAmount)

        binding.textChangeAmount.text = CalculatorHelper.formatCurrency(change)

        // Update UI based on validation
        if (isValid) {
            binding.textChangeAmount.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            )
            binding.textChangeLabel.text = "Kembalian:"
        } else {
            binding.textChangeAmount.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
            binding.textChangeLabel.text = "Kurang:"
            binding.textChangeAmount.text = CalculatorHelper.formatCurrency(totalAmount - paidAmount)
        }
    }

    private fun updatePaymentMethodUI() {
        when (selectedPaymentMethod) {
            PaymentMethod.CASH -> {
                binding.layoutCashPayment.visibility = View.VISIBLE
            }
            PaymentMethod.TRANSFER -> {
                binding.layoutCashPayment.visibility = View.GONE
                binding.editTextPaidAmount.setText(totalAmount.toString())
            }
            PaymentMethod.QRISS -> {
                binding.layoutCashPayment.visibility = View.GONE
                binding.editTextPaidAmount.setText(totalAmount.toString())
            }
        }
    }

    private fun processPayment() {
        val paidAmountText = binding.editTextPaidAmount.text.toString()
        val paidAmount = paidAmountText.toDoubleOrNull() ?: 0.0

        if (!CalculatorHelper.isValidPayment(totalAmount, paidAmount)) {
            return // Dialog will stay open
        }

        onPaymentConfirmed(selectedPaymentMethod, paidAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

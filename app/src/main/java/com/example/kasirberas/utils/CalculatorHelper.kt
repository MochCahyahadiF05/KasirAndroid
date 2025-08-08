package com.example.kasirberas.utils

import java.text.NumberFormat
import java.util.*

object CalculatorHelper {

    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    fun formatNumber(number: Double): String {
        return String.format("%,.0f", number)
    }

    fun calculateTotal(items: List<com.example.kasirberas.data.model.CartItem>): Double {
        return items.sumOf { it.subtotal }
    }

    fun calculateChange(totalAmount: Double, paidAmount: Double): Double {
        return maxOf(0.0, paidAmount - totalAmount)
    }

    fun isValidPayment(totalAmount: Double, paidAmount: Double): Boolean {
        return paidAmount >= totalAmount
    }
}

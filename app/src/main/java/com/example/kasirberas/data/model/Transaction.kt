package com.example.kasirberas.data.model

data class Transaction(
    val id: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val changeAmount: Double = 0.0,
    val transactionDate: Long = System.currentTimeMillis(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
){
    val formattedTotal: String
        get() = "Rp ${String.format("%,.0f", totalAmount)}"
}
enum class PaymentMethod(val displayName: String) {
    CASH("Tunai"),
    TRANSFER("Transfer"),
    QRISS("Qriss")
}

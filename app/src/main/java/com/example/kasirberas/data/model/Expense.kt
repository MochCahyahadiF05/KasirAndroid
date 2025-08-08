package com.example.kasirberas.data.model

data class Expense(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val status: ExpenseStatus = ExpenseStatus.BELUM_BAYAR,
    val dueDate: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long = 0
){
    val formattedAmount: String
        get() = "Rp ${String.format("%,.0f", amount)}"

    val statusColor: Int
        get() = when(status) {
            ExpenseStatus.BELUM_BAYAR -> android.R.color.holo_red_light
            ExpenseStatus.CICIL -> android.R.color.holo_orange_light
            ExpenseStatus.LUNAS -> android.R.color.holo_green_light
        }
}
enum class ExpenseStatus(val displayName: String) {
    BELUM_BAYAR("Belum Bayar"),
    CICIL("Cicil"),
    LUNAS("Lunas")
}


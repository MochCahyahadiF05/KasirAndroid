package com.example.kasirberas.data.model

data class CartItem(
    val id: String = "",
    val product: Product,
    var quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
) {
    val subtotal: Double
        get() = product.price * quantity

    val formattedSubtotal: String
        get() = "Rp ${String.format("%,.0f", subtotal)}"

}

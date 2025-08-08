package com.example.kasirberas.data.model

import java.io.Serializable

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: ProductCategory = ProductCategory.BERAS,
    val unit: String = "kg",
    val stock: Int = 0,
    val imagePath: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()

):Serializable
enum class ProductCategory(val displayName: String) {
    BERAS("Beras"),
    TELUR("Telur"),
    GAS_3KG("Gas 3kg"),
    LAINNYA("Lainnya")
}


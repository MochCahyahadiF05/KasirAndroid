package com.example.kasirberas.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Product
class CartRepository {

    private val _cartItems = MutableLiveData<MutableList<CartItem>>(mutableListOf())
    val cartItems: LiveData<MutableList<CartItem>> = _cartItems

    private val _totalAmount = MutableLiveData<Double>(0.0)
    val totalAmount: LiveData<Double> = _totalAmount

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> = _itemCount

    companion object {
        @Volatile
        private var INSTANCE: CartRepository? = null

        fun getInstance(): CartRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CartRepository().also { INSTANCE = it }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentItems = _cartItems.value ?: mutableListOf()

        // Check if product already in cart
        val existingItem = currentItems.find { it.product.id == product.id }

        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            val cartId = "cart_${System.currentTimeMillis()}"
            val newItem = CartItem(cartId, product, quantity)
            currentItems.add(newItem)
        }

        _cartItems.value = currentItems
        updateCalculations()
    }

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        val currentItems = _cartItems.value ?: mutableListOf()

        if (newQuantity <= 0) {
            removeFromCart(cartItemId)
            return
        }

        currentItems.find { it.id == cartItemId }?.quantity = newQuantity
        _cartItems.value = currentItems
        updateCalculations()
    }

    fun removeFromCart(cartItemId: String) {
        val currentItems = _cartItems.value ?: mutableListOf()
        currentItems.removeAll { it.id == cartItemId }
        _cartItems.value = currentItems
        updateCalculations()
    }

    fun clearCart() {
        _cartItems.value = mutableListOf()
        updateCalculations()
    }

    private fun updateCalculations() {
        val items = _cartItems.value ?: mutableListOf()
        val total = items.sumOf { it.subtotal }
        val count = items.sumOf { it.quantity }

        _totalAmount.value = total
        _itemCount.value = count
    }

    fun getCartItems(): List<CartItem> {
        return _cartItems.value?.toList() ?: emptyList()
    }

    fun getCartItemCount(): Int {
        return _itemCount.value ?: 0
    }

    fun getTotalAmount(): Double {
        return _totalAmount.value ?: 0.0
    }
}


package com.example.kasirberas.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.data.model.PaymentMethod
import com.example.kasirberas.data.repository.CartRepository
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.FirestoreHelper

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository.getInstance()
    private val firestoreHelper = FirestoreHelper()

    private val _checkoutResult = MutableLiveData<Boolean>()
    val checkoutResult: LiveData<Boolean> = _checkoutResult

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    // Tambahan LiveData agar sesuai dengan CartFragment
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _paymentSuccess = MutableLiveData<Boolean>()
    val paymentSuccess: LiveData<Boolean> = _paymentSuccess

    // Expose cart data from repository
    val cartItems = cartRepository.cartItems
    val totalAmount = cartRepository.totalAmount
    val itemCount = cartRepository.itemCount

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        cartRepository.updateQuantity(cartItemId, newQuantity)
    }

    fun removeFromCart(cartItemId: String) {
        cartRepository.removeFromCart(cartItemId)
        _message.value = "Item dihapus dari keranjang"
    }

    fun clearCart() {
        cartRepository.clearCart()
        _message.value = "Keranjang dikosongkan"
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearPaymentSuccess() {
        _paymentSuccess.value = false
    }

    // Digunakan di PaymentDialog -> proses pembayaran
    fun processPayment(paymentMethod: PaymentMethod, paidAmount: Double) {
        checkout(paidAmount, paymentMethod)
    }

    fun checkout(paidAmount: Double, paymentMethod: PaymentMethod) {
        val items = cartRepository.getCartItems()
        val total = cartRepository.getTotalAmount()

        if (items.isEmpty()) {
            _errorMessage.value = "Keranjang kosong"
            return
        }

        if (!CalculatorHelper.isValidPayment(total, paidAmount)) {
            _errorMessage.value = "Jumlah pembayaran tidak mencukupi"
            return
        }

        _isLoading.value = true

        val change = CalculatorHelper.calculateChange(total, paidAmount)

        val transaction = Transaction(
            items = items,
            totalAmount = total,
            paidAmount = paidAmount,
            changeAmount = change,
            paymentMethod = paymentMethod
        )

        firestoreHelper.addTransaction(transaction) { success ->
            _isLoading.value = false
            if (success) {
                cartRepository.clearCart()
                _paymentSuccess.value = true
                _message.value =
                    "Transaksi berhasil! Kembalian: ${CalculatorHelper.formatCurrency(change)}"
            } else {
                _paymentSuccess.value = false
                _errorMessage.value = "Transaksi gagal"
            }
        }
    }
}

package com.example.kasirberas.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kasirberas.data.model.Product
import com.example.kasirberas.data.repository.CartRepository
import com.example.kasirberas.utils.FirestoreHelper
class ProductViewModel : ViewModel() {

    private val firestoreHelper = FirestoreHelper()
    private val cartRepository = CartRepository.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _filteredProducts = MutableLiveData<List<Product>>()
    val filteredProducts: LiveData<List<Product>> = _filteredProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    init {
        loadProducts()
    }

    fun loadProducts() {
        _isLoading.value = true
        firestoreHelper.getAllProducts { products ->
            _products.value = products
            _filteredProducts.value = products
            _isLoading.value = false
        }
    }

    fun addProduct(product: Product) {
        _isLoading.value = true
        firestoreHelper.addProduct(product) { success ->
            _isLoading.value = false
            if (success) {
                _message.value = "Produk berhasil ditambahkan"
                loadProducts()
            } else {
                _message.value = "Gagal menambah produk"
            }
        }
    }

    fun deleteProduct(product: Product) {
        _isLoading.value = true
        firestoreHelper.deleteProduct(product.id) { success ->
            _isLoading.value = false
            if (success) {
                _message.value = "Produk berhasil dihapus"
                loadProducts()
            } else {
                _message.value = "Gagal menghapus produk"
            }
        }
    }

    fun addToCart(product: Product) {
        if (product.stock > 0) {
            cartRepository.addToCart(product)
            _message.value = "${product.name} ditambahkan ke keranjang"
        } else {
            _message.value = "Stok ${product.name} habis"
        }
    }

    fun searchProducts(query: String) {
        val allProducts = _products.value ?: return

        if (query.isEmpty()) {
            _filteredProducts.value = allProducts
        } else {
            val filtered = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.category.displayName.contains(query, ignoreCase = true)
            }
            _filteredProducts.value = filtered
        }
    }
}

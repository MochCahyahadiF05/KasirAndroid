package com.example.kasirberas.ui.cart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasirberas.R
import com.example.kasirberas.adapter.CartAdapter
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.databinding.FragmentCartBinding
import com.example.kasirberas.dialog.PaymentDialog
import com.example.kasirberas.dialog.ReceiptDialog
import com.example.kasirberas.utils.CalculatorHelper

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    // Permission launcher untuk storage
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            showToast("Izin penyimpanan diperlukan untuk menyimpan struk")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        checkStoragePermissions()
    }

    private fun initializeViewModel() {
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { cartItemId, newQuantity ->
                cartViewModel.updateQuantity(cartItemId, newQuantity)
            },
            onRemoveClick = { cartItemId ->
                cartViewModel.removeFromCart(cartItemId)
            }
        )

        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun setupObservers() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            cartAdapter.submitList(cartItems)

            val isEmpty = cartItems.isEmpty()
            binding.recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.cardBottomSummary.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        cartViewModel.totalAmount.observe(viewLifecycleOwner) { totalAmount ->
            binding.textTotalAmount.text = CalculatorHelper.formatCurrency(totalAmount)
        }

        cartViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showToast(it)
                cartViewModel.clearErrorMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonCheckout.setOnClickListener {
            performCheckout()
        }
        binding.buttonClearCart.setOnClickListener {
            cartViewModel.clearCart()
            showToast("Keranjang dikosongkan")
        }
        binding.buttonContinueShopping.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun performCheckout() {
        val cartItems = cartViewModel.cartItems.value ?: emptyList()
        val totalAmount = cartViewModel.totalAmount.value ?: 0.0

        if (cartItems.isEmpty()) {
            showToast("Keranjang kosong")
            return
        }

        val paymentDialog = PaymentDialog(
            context = requireContext(),
            cartItems = cartItems,
            totalAmount = totalAmount,
            onPaymentSuccess = { transaction, paymentAmount, change ->
                handlePaymentSuccess(transaction, cartItems, paymentAmount, change)
            }
        )

        paymentDialog.show()
    }

    private fun handlePaymentSuccess(
        transaction: Transaction,
        cartItems: List<com.example.kasirberas.data.model.CartItem>,
        paymentAmount: Double,
        change: Double
    ) {
        cartViewModel.clearCart()
        showReceiptDialog(transaction, cartItems, paymentAmount, change)
        showToast("Pembayaran berhasil!")
    }

    private fun showReceiptDialog(
        transaction: Transaction,
        cartItems: List<com.example.kasirberas.data.model.CartItem>,
        paymentAmount: Double,
        change: Double
    ) {
        val receiptDialog = ReceiptDialog(
            context = requireContext(),
            transaction = transaction,
            cartItems = cartItems,
            paymentAmount = paymentAmount,
            change = change,
            storeName = "TOKO KASIR",
            storeAddress = "Jl. Contoh No. 123, Banjar",
            cashierName = "Admin"
        )

        receiptDialog.show()
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            if (permissions.isNotEmpty()) {
                storagePermissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.kasirberas.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasirberas.adapter.CartAdapter
import com.example.kasirberas.databinding.FragmentCartBinding
import com.example.kasirberas.dialog.PaymentDialog
import com.example.kasirberas.utils.CalculatorHelper

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartViewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
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
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)

            val isEmpty = items.isEmpty()
            binding.recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.cardBottomSummary.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        cartViewModel.itemCount.observe(viewLifecycleOwner) { count ->
            binding.textCartItemCount.text = "$count item"
        }

        cartViewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            binding.textTotalAmount.text = CalculatorHelper.formatCurrency(total)
        }

        cartViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        cartViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                cartViewModel.clearErrorMessage()
            }
        }

        cartViewModel.paymentSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Payment successful! Receipt generated.", Toast.LENGTH_LONG).show()
                cartViewModel.clearPaymentSuccess()
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonCheckout.setOnClickListener {
            handleCheckout()
        }

        binding.buttonClearCart.setOnClickListener {
            cartViewModel.clearCart()
            Toast.makeText(context, "Cart cleared", Toast.LENGTH_SHORT).show()
        }

        binding.buttonContinueShopping.setOnClickListener {
            // Navigate back to products fragment
            parentFragmentManager.popBackStack()
        }
    }

    private fun handleCheckout() {
        val totalAmount = cartViewModel.totalAmount.value ?: 0.0

        if (totalAmount <= 0) {
            Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentDialog = PaymentDialog(
            totalAmount = totalAmount,
            onPaymentConfirmed = { paymentMethod, amountPaid ->
                cartViewModel.processPayment(paymentMethod, amountPaid)
            }
        )

        paymentDialog.show(parentFragmentManager, "PaymentDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

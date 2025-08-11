package com.example.kasirberas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirberas.R
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.ImageHelper

class CartAdapter(
    private val onQuantityChange: (String, Int) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    private lateinit var imageHelper: ImageHelper

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        imageHelper = ImageHelper(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        private val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        private val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        private val textSubtotal: TextView = itemView.findViewById(R.id.textSubtotal)
        private val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        private val buttonDecrease: Button = itemView.findViewById(R.id.buttonDecrease)
        private val buttonIncrease: Button = itemView.findViewById(R.id.buttonIncrease)
        private val buttonRemove: Button = itemView.findViewById(R.id.buttonRemove)

        fun bind(cartItem: CartItem) {
            textProductName.text = cartItem.product.name
            textProductPrice.text =
                "${CalculatorHelper.formatCurrency(cartItem.product.price)} / ${cartItem.product.unit}"
            textSubtotal.text = CalculatorHelper.formatCurrency(cartItem.subtotal)
            textQuantity.text = cartItem.quantity.toString()

            // Load image
            imageHelper.loadImageIntoView(cartItem.product.imagePath, imageProduct)

            // Tombol minus
            buttonDecrease.setOnClickListener {
                if (cartItem.quantity > 1) {
                    val newQuantity = cartItem.quantity - 1
                    // Update tampilan quantity
                    textQuantity.text = newQuantity.toString()
                    // ✅ Update tampilan subtotal
                    val newSubtotal = cartItem.product.price * newQuantity
                    textSubtotal.text = CalculatorHelper.formatCurrency(newSubtotal)
                    // Kirim ke parent untuk update data
                    onQuantityChange(cartItem.id, newQuantity)
                }
            }

            // Tombol plus
            buttonIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                // Update tampilan quantity
                textQuantity.text = newQuantity.toString()
                // ✅ Update tampilan subtotal
                val newSubtotal = cartItem.product.price * newQuantity
                textSubtotal.text = CalculatorHelper.formatCurrency(newSubtotal)
                // Kirim ke parent untuk update data
                onQuantityChange(cartItem.id, newQuantity)
            }

            // Tombol hapus
            buttonRemove.setOnClickListener {
                onRemoveClick(cartItem.id)
            }
        }
    }

    class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}

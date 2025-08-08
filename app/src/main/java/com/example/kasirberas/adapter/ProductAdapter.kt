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
import com.example.kasirberas.data.model.Product
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.ImageHelper

class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    private lateinit var imageHelper: ImageHelper
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        imageHelper = ImageHelper(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        private val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        private val textProductCategory: TextView = itemView.findViewById(R.id.textProductCategory)
        private val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        private val textStock: TextView = itemView.findViewById(R.id.textStock)
        private val buttonAddToCart: Button = itemView.findViewById(R.id.buttonAddToCart)
        fun bind(product: Product) {
            textProductName.text = product.name
            textProductCategory.text = product.category.displayName
            textProductPrice.text = CalculatorHelper.formatCurrency(product.price)
            textStock.text = "Stock: ${product.stock} ${product.unit}"
// Load image
            imageHelper.loadImageIntoView(product.imagePath, imageProduct)
// Set click listeners
            itemView.setOnClickListener { onItemClick(product) }
            buttonAddToCart.setOnClickListener { onAddToCartClick(product) }
// Disable add to cart if out of stock
            buttonAddToCart.isEnabled = product.stock > 0
            buttonAddToCart.text = if (product.stock > 0) "+ Keranjang" else "Habis"
        }
    }
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}

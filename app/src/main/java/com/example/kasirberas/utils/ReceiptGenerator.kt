package com.example.kasirberas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.kasirberas.R
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReceiptGenerator(private val context: Context) {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        currency = Currency.getInstance("IDR")
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    /**
     * Generate bitmap dari data transaksi
     */
    fun generateReceiptBitmap(
        transaction: Transaction,
        cartItems: List<CartItem>,
        paymentAmount: Double,
        change: Double,
        storeName: String = "TOKO KASIR",
        storeAddress: String = "Jl. Contoh No. 123, Banjar",
        cashierName: String = "Admin"
    ): Bitmap {

        val receiptView = createReceiptView(
            transaction, cartItems, paymentAmount, change,
            storeName, storeAddress, cashierName
        )

        return viewToBitmap(receiptView)
    }

    /**
     * Create view untuk struk
     */
    private fun createReceiptView(
        transaction: Transaction,
        cartItems: List<CartItem>,
        paymentAmount: Double,
        change: Double,
        storeName: String,
        storeAddress: String,
        cashierName: String
    ): View {

        val inflater = LayoutInflater.from(context)
        val receiptView = inflater.inflate(R.layout.layout_receipt, null)

        // Set store info
        receiptView.findViewById<TextView>(R.id.tvStoreName).text = storeName
        receiptView.findViewById<TextView>(R.id.tvStoreAddress).text = storeAddress

        // Set transaction info
        receiptView.findViewById<TextView>(R.id.tvTransactionId).text = transaction.id

        // Fix: Use transactionDate (Long) instead of date
        receiptView.findViewById<TextView>(R.id.tvTransactionDate).text = dateFormat.format(Date(transaction.transactionDate))
        receiptView.findViewById<TextView>(R.id.tvCashier).text = cashierName

        // Add items
        val itemContainer = receiptView.findViewById<LinearLayout>(R.id.llItemList)
        cartItems.forEach { item ->
            val itemView = createItemView(item)
            itemContainer.addView(itemView)
        }

        // Set totals
        receiptView.findViewById<TextView>(R.id.tvSubtotal).text = formatCurrency(transaction.totalAmount)
        receiptView.findViewById<TextView>(R.id.tvTotal).text = formatCurrency(transaction.totalAmount)
        receiptView.findViewById<TextView>(R.id.tvPaymentAmount).text = formatCurrency(paymentAmount)
        receiptView.findViewById<TextView>(R.id.tvChange).text = formatCurrency(change)

        return receiptView
    }

    /**
     * Create view untuk item individual
     */
    private fun createItemView(cartItem: CartItem): View {
        val itemLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
        }

        // Item name
        val tvItemName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            text = cartItem.product.name
            textSize = 12f
            setTextColor(Color.BLACK)
        }

        // Quantity
        val tvQuantity = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = cartItem.quantity.toString()
            textSize = 12f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.CENTER
        }

        // Unit price
        val tvUnitPrice = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = formatCurrency(cartItem.product.price)
            textSize = 12f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.END
        }

        // Total price - Use existing subtotal property
        val tvTotalPrice = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = formatCurrency(cartItem.subtotal)
            textSize = 12f
            setTextColor(Color.BLACK)
            gravity = android.view.Gravity.END
        }

        itemLayout.addView(tvItemName)
        itemLayout.addView(tvQuantity)
        itemLayout.addView(tvUnitPrice)
        itemLayout.addView(tvTotalPrice)

        return itemLayout
    }

    /**
     * Convert view menjadi bitmap
     */
    private fun viewToBitmap(view: View): Bitmap {
        // Measure dan layout view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create bitmap
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        // Draw view ke bitmap
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        return bitmap
    }

    /**
     * Format currency
     */
    private fun formatCurrency(amount: Double): String {
        return numberFormat.format(amount).replace("IDR", "Rp")
    }

    /**
     * Get receipt view untuk preview di dialog
     */
    fun getReceiptView(
        transaction: Transaction,
        cartItems: List<CartItem>,
        paymentAmount: Double,
        change: Double,
        storeName: String = "TOKO KASIR",
        storeAddress: String = "Jl. Contoh No. 123, Banjar",
        cashierName: String = "Admin"
    ): View {
        return createReceiptView(
            transaction, cartItems, paymentAmount, change,
            storeName, storeAddress, cashierName
        )
    }
}
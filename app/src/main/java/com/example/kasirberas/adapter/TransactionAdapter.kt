package com.example.kasirberas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kasirberas.R
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.DateHelper

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTransactionId: TextView = itemView.findViewById(R.id.textTransactionId)
        private val textTransactionDate: TextView = itemView.findViewById(R.id.textTransactionDate)
        private val textTotalAmount: TextView = itemView.findViewById(R.id.textTotalAmount)
        private val textItemCount: TextView = itemView.findViewById(R.id.textItemCount)
        private val textPaymentMethod: TextView = itemView.findViewById(R.id.textPaymentMethod)
        private val imageExpand: ImageView = itemView.findViewById(R.id.imageExpand)
        private val layoutItemDetails: ViewGroup = itemView.findViewById(R.id.layoutItemDetails)
        private val textItemDetails: TextView = itemView.findViewById(R.id.textItemDetails)

        private var isExpanded = false

        fun bind(transaction: Transaction) {
            // Format transaction ID untuk display
            textTransactionId.text = "TRX-${transaction.id.takeLast(8).uppercase()}"
            textTransactionDate.text = DateHelper.formatDateTime(transaction.transactionDate)
            textTotalAmount.text = CalculatorHelper.formatCurrency(transaction.totalAmount)
            textItemCount.text = "${transaction.items.size} item"
            textPaymentMethod.text = transaction.paymentMethod.displayName

            // Setup item details
            val itemDetails = transaction.items.joinToString("\n") { cartItem ->
                "• ${cartItem.product.name} x${cartItem.quantity} = ${CalculatorHelper.formatCurrency(cartItem.subtotal)}"
            }
            textItemDetails.text = itemDetails

            // Setup expand/collapse
            updateExpandState()

            // Click listeners
            itemView.setOnClickListener {
                toggleExpand()
                onItemClick(transaction)
            }

            imageExpand.setOnClickListener {
                toggleExpand()
            }
        }

        private fun toggleExpand() {
            isExpanded = !isExpanded
            updateExpandState()
        }

        private fun updateExpandState() {
            if (isExpanded) {
                layoutItemDetails.visibility = View.VISIBLE
                imageExpand.setImageResource(R.drawable.ic_expand_less_24)
            } else {
                layoutItemDetails.visibility = View.GONE
                imageExpand.setImageResource(R.drawable.ic_expand_more_24)
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
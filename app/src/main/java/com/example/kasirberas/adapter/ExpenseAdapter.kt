package com.example.kasirberas.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.kasirberas.R
import com.example.kasirberas.data.model.Expense
import com.example.kasirberas.data.model.ExpenseStatus
import com.example.kasirberas.utils.CalculatorHelper
import com.example.kasirberas.utils.DateHelper

class ExpenseAdapter(
    private val onStatusClick: (Expense) -> Unit,
    private val onItemLongClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardExpense)
        private val textExpenseName: TextView = itemView.findViewById(R.id.textExpenseName)
        private val textExpenseAmount: TextView = itemView.findViewById(R.id.textExpenseAmount)
        private val textExpenseDescription: TextView = itemView.findViewById(R.id.textExpenseDescription)
        private val textCreatedDate: TextView = itemView.findViewById(R.id.textCreatedDate)
        private val textDueDate: TextView = itemView.findViewById(R.id.textDueDate)
        private val buttonStatus: MaterialButton = itemView.findViewById(R.id.buttonStatus)
        private val viewStatusIndicator: View = itemView.findViewById(R.id.viewStatusIndicator)

        fun bind(expense: Expense) {
            textExpenseName.text = expense.name
            textExpenseAmount.text = CalculatorHelper.formatCurrency(expense.amount)
            textCreatedDate.text = "Dibuat: ${DateHelper.formatDate(expense.createdAt)}"

            // Description
            if (expense.description.isNotEmpty()) {
                textExpenseDescription.text = expense.description
                textExpenseDescription.visibility = View.VISIBLE
            } else {
                textExpenseDescription.visibility = View.GONE
            }

            // Due date
            if (expense.dueDate > 0) {
                textDueDate.text = "Jatuh tempo: ${DateHelper.formatDate(expense.dueDate)}"
                textDueDate.visibility = View.VISIBLE

                // Check if overdue
                if (expense.dueDate < System.currentTimeMillis() && expense.status != ExpenseStatus.LUNAS) {
                    textDueDate.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                } else {
                    textDueDate.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                }
            } else {
                textDueDate.visibility = View.GONE
            }

            // Status setup
            setupStatus(expense.status)

            // Click listeners
            buttonStatus.setOnClickListener {
                onStatusClick(expense)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(expense)
                true
            }
        }

        private fun setupStatus(status: ExpenseStatus) {
            buttonStatus.text = status.displayName

            val (backgroundColor, textColor) = when (status) {
                ExpenseStatus.BELUM_BAYAR -> {
                    Pair(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_red_light),
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                }
                ExpenseStatus.CICIL -> {
                    Pair(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light),
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                }
                ExpenseStatus.LUNAS -> {
                    Pair(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_green_light),
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                }
            }

            buttonStatus.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            buttonStatus.setTextColor(textColor)
            viewStatusIndicator.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
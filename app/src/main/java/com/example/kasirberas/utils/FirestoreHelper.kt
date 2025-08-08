package com.example.kasirberas.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.kasirberas.data.model.*

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    //product
    fun addProduct(product: Product, onComplete: (Boolean) -> Unit) {
        val productId = if (product.id.isEmpty()) db.collection("products").document().id else product.id
        val productWithId = product.copy(id = productId)

        db.collection("products")
            .document(productId)
            .set(productWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllProducts(onResult: (List<Product>) -> Unit) {
        db.collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val products = documents.mapNotNull { it.toObject(Product::class.java) }
                onResult(products)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit) {
        db.collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Transactions
    fun addTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        val transactionId = if (transaction.id.isEmpty()) db.collection("transactions").document().id else transaction.id
        val transactionWithId = transaction.copy(id = transactionId)

        db.collection("transactions")
            .document(transactionId)
            .set(transactionWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllTransactions(onResult: (List<Transaction>) -> Unit) {
        db.collection("transactions")
            .orderBy("transactionDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.mapNotNull { it.toObject(Transaction::class.java) }
                onResult(transactions)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // Expenses
    fun addExpense(expense: Expense, onComplete: (Boolean) -> Unit) {
        val expenseId = if (expense.id.isEmpty()) db.collection("expenses").document().id else expense.id
        val expenseWithId = expense.copy(id = expenseId)

        db.collection("expenses")
            .document(expenseId)
            .set(expenseWithId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getAllExpenses(onResult: (List<Expense>) -> Unit) {
        db.collection("expenses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.mapNotNull { it.toObject(Expense::class.java) }
                onResult(expenses)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun updateExpenseStatus(expenseId: String, status: ExpenseStatus, onComplete: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "paidAt" to if (status == ExpenseStatus.LUNAS) System.currentTimeMillis() else 0
        )

        db.collection("expenses")
            .document(expenseId)
            .update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
    fun deleteExpense(expenseId: String, onComplete: (Boolean) -> Unit) {
        db.collection("expenses")
            .document(expenseId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
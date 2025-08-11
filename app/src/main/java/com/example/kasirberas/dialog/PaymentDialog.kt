package com.example.kasirberas.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.example.kasirberas.R
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.data.model.PaymentMethod
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentDialog(
    context: Context,
    private val cartItems: List<CartItem>,
    private val totalAmount: Double,
    private val onPaymentSuccess: (transaction: Transaction, paymentAmount: Double, change: Double) -> Unit
) : Dialog(context, R.style.CustomDialogAnimation) {

    private lateinit var tvTotalAmount: TextView
    private lateinit var etPaymentAmount: EditText
    private lateinit var tvChangeAmount: TextView
    private lateinit var btnConfirmPayment: Button
    private lateinit var btnCancel: Button
    private lateinit var radioGroupPayment: RadioGroup

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        currency = Currency.getInstance("IDR")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_payment)

        initializeViews()
        setupInitialData()
        setupClickListeners()

        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

    private fun initializeViews() {
        tvTotalAmount = findViewById(R.id.textTotalAmount)
        etPaymentAmount = findViewById(R.id.etPaymentAmount)
        tvChangeAmount = findViewById(R.id.tvChangeAmount)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)
        btnCancel = findViewById(R.id.btnCancel)
        radioGroupPayment = findViewById(R.id.radioGroupPayment)
    }

    private fun setupInitialData() {
        tvTotalAmount.text = formatCurrency(totalAmount)
        tvChangeAmount.text = formatCurrency(0.0)

        etPaymentAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateChange()
            }
        })
    }

    private fun setupClickListeners() {
        btnConfirmPayment.setOnClickListener {
            processPayment()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        findViewById<MaterialButton>(R.id.buttonExactAmount)?.setOnClickListener {
            etPaymentAmount.setText(totalAmount.toString())
            etPaymentAmount.setSelection(etPaymentAmount.text.length)
        }

        // ✅ Tambahkan listener untuk sembunyikan/tampilkan input tunai
        radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            val cashLayout = findViewById<LinearLayout>(R.id.layoutCashPayment)
            if (checkedId == R.id.radioButtonCash) {
                cashLayout.visibility = View.VISIBLE
            } else {
                cashLayout.visibility = View.GONE
            }
        }
    }

    private fun calculateChange() {
        val paymentText = etPaymentAmount.text.toString()

        if (paymentText.isNotEmpty()) {
            try {
                val paymentAmount = paymentText.toDouble()
                val change = paymentAmount - totalAmount

                tvChangeAmount.text = formatCurrency(change)
                btnConfirmPayment.isEnabled = paymentAmount >= totalAmount

                if (change >= 0) {
                    tvChangeAmount.setTextColor(context.getColor(android.R.color.holo_green_dark))
                } else {
                    tvChangeAmount.setTextColor(context.getColor(android.R.color.holo_red_dark))
                }

            } catch (e: NumberFormatException) {
                tvChangeAmount.text = formatCurrency(0.0)
                btnConfirmPayment.isEnabled = false
            }
        } else {
            tvChangeAmount.text = formatCurrency(0.0)
            btnConfirmPayment.isEnabled = false
        }
    }

    private fun processPayment() {
        val selectedPaymentId = radioGroupPayment.checkedRadioButtonId
        if (selectedPaymentId == -1) {
            showToast("Silakan pilih metode pembayaran")
            return
        }

        val paymentMethod = when (selectedPaymentId) {
            R.id.radioButtonCash -> PaymentMethod.CASH
            R.id.radioButtonTransfer -> PaymentMethod.TRANSFER
            else -> PaymentMethod.CASH
        }

        var paymentAmount: Double
        var change: Double

        if (paymentMethod == PaymentMethod.CASH) {
            val paymentText = etPaymentAmount.text.toString()
            if (paymentText.isEmpty()) {
                showToast("Masukkan jumlah pembayaran")
                return
            }
            try {
                paymentAmount = paymentText.toDouble()
            } catch (e: NumberFormatException) {
                showToast("Format pembayaran tidak valid")
                return
            }
            if (paymentAmount < totalAmount) {
                showToast("Jumlah pembayaran tidak mencukupi")
                return
            }
            change = paymentAmount - totalAmount
        } else {
            paymentAmount = totalAmount
            change = 0.0
        }

        val transaction = Transaction(
            id = generateTransactionId(),
            transactionDate = System.currentTimeMillis(),
            totalAmount = totalAmount,
            paidAmount = paymentAmount,
            changeAmount = change,
            items = cartItems.map { it.copy() },
            paymentMethod = paymentMethod
        )

        // ✅ Simpan transaksi ke Firestore - disesuaikan dengan struktur CartItem baru
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val transactionMap = hashMapOf(
            "id" to transaction.id,
            "transactionDate" to transaction.transactionDate,
            "totalAmount" to transaction.totalAmount,
            "paidAmount" to transaction.paidAmount,
            "changeAmount" to transaction.changeAmount,
            "paymentMethod" to transaction.paymentMethod.name,
            "items" to transaction.items.map { cartItem ->
                mapOf(
                    "id" to cartItem.id,
                    "product" to mapOf(
                        "id" to cartItem.product.id,
                        "name" to cartItem.product.name,
                        "category" to cartItem.product.category,
                        "price" to cartItem.product.price
                    ),
                    "quantity" to cartItem.quantity,
                    "subtotal" to cartItem.subtotal,
                    "formattedSubtotal" to cartItem.formattedSubtotal,
                    "addedAt" to cartItem.addedAt
                )
            }
        )

        db.collection("transactions")
            .add(transactionMap)
            .addOnSuccessListener {
                showToast("Pembayaran berhasil dan data tersimpan")
                onPaymentSuccess(transaction, paymentAmount, change)
                dismiss()
            }
            .addOnFailureListener { e ->
                showToast("Pembayaran berhasil, tapi gagal simpan data: ${e.message}")
                onPaymentSuccess(transaction, paymentAmount, change)
                dismiss()
            }
    }

    // Simpan struk sebagai file text
    private fun saveReceiptText(transaction: Transaction): Boolean {
        return try {
            val fileName = "Struk_${transaction.id}.txt"
            val file = File(context.getExternalFilesDir(null), fileName)

            val receiptContent = buildString {
                appendLine("=".repeat(40))
                appendLine("           STRUK TRANSAKSI")
                appendLine("=".repeat(40))
                appendLine("ID Transaksi: ${transaction.id}")
                appendLine("Tanggal: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(transaction.transactionDate))}")
                appendLine("-".repeat(40))

                transaction.items.forEach { item ->
                    appendLine("${item.product.name}")
                    appendLine("${item.quantity} x ${formatCurrency(item.product.price)} = ${item.formattedSubtotal}")
                    appendLine()
                }

                appendLine("-".repeat(40))
                appendLine("TOTAL: ${formatCurrency(transaction.totalAmount)}")
                appendLine("BAYAR: ${formatCurrency(transaction.paidAmount)}")
                appendLine("KEMBALI: ${formatCurrency(transaction.changeAmount)}")
                appendLine("METODE: ${transaction.paymentMethod.name}")
                appendLine("=".repeat(40))
                appendLine("    Terima kasih atas kunjungan Anda!")
                appendLine("=".repeat(40))
            }

            FileOutputStream(file).use { fos ->
                fos.write(receiptContent.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun generateTransactionId(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return "TRX${dateFormat.format(Date())}"
    }

    private fun formatCurrency(amount: Double): String {
        return numberFormat.format(amount).replace("IDR", "Rp")
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
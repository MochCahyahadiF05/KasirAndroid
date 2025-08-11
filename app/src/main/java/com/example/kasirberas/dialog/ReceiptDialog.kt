package com.example.kasirberas.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.example.kasirberas.R
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Transaction
import com.example.kasirberas.utils.FileHelper
import com.example.kasirberas.utils.PrintHelper
import com.example.kasirberas.utils.ReceiptGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiptDialog(
    context: Context,
    private val transaction: Transaction,
    private val cartItems: List<CartItem>,
    private val paymentAmount: Double,
    private val change: Double,
    private val storeName: String = "TOKO KASIR",
    private val storeAddress: String = "Jl. Contoh No. 123, Banjar",
    private val cashierName: String = "Admin"
) : Dialog(context, R.style.CustomMaterialDialog) {

    private lateinit var receiptGenerator: ReceiptGenerator
    private lateinit var fileHelper: FileHelper
    private lateinit var printHelper: PrintHelper
    private var receiptBitmap: Bitmap? = null

    // UI Components
    private lateinit var btnClose: ImageButton
    private lateinit var btnSaveImage: MaterialButton
    private lateinit var btnDownload: MaterialButton
    private lateinit var btnPrint: MaterialButton
    private lateinit var llReceiptContainer: LinearLayout

    // Handler untuk UI thread
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_receipt)

        initializeHelpers()
        initializeViews()
        setupReceiptView()
        setupClickListeners()

        // Set dialog properties
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        // Set dialog size
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.95).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.9).toInt()
        )
    }

    private fun initializeHelpers() {
        receiptGenerator = ReceiptGenerator(context)
        fileHelper = FileHelper(context)
        printHelper = PrintHelper(context)
    }

    private fun initializeViews() {
        btnClose = findViewById(R.id.btnClose)
        btnSaveImage = findViewById(R.id.btnSaveImage)
        btnDownload = findViewById(R.id.btnDownload)
        btnPrint = findViewById(R.id.btnPrint)
        llReceiptContainer = findViewById(R.id.llReceiptContainer)
    }

    private fun setupReceiptView() {
        val receiptView = receiptGenerator.getReceiptView(
            transaction, cartItems, paymentAmount, change,
            storeName, storeAddress, cashierName
        )

        llReceiptContainer.removeAllViews()
        llReceiptContainer.addView(receiptView)

        generateReceiptBitmap()
    }

    private fun generateReceiptBitmap() {
        Thread {
            try {
                receiptBitmap = receiptGenerator.generateReceiptBitmap(
                    transaction, cartItems, paymentAmount, change,
                    storeName, storeAddress, cashierName
                )
                mainHandler.post {
                    showToast("Struk siap untuk disimpan")
                }
            } catch (e: Exception) {
                mainHandler.post {
                    showToast("Gagal memproses struk: ${e.message}")
                }
            }
        }.start()
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            dismiss()
        }

        btnSaveImage.setOnClickListener {
            saveReceiptAsImage()
        }

        btnDownload.setOnClickListener {
            downloadReceiptImage()
        }

        btnPrint.setOnClickListener {
            printReceipt()
        }
    }

    private fun saveReceiptAsImage() {
        if (receiptBitmap == null) {
            showToast("Sedang memproses struk, silakan coba lagi...")
            generateReceiptBitmap()
            return
        }

        btnSaveImage.isEnabled = false
        btnSaveImage.text = "Menyimpan..."

        if (context is androidx.lifecycle.LifecycleOwner) {
            (context as androidx.lifecycle.LifecycleOwner).lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        fileHelper.saveBitmapToGallery(receiptBitmap!!, transaction.id)
                    }
                    result.onSuccess { message ->
                        showToast("✅ $message")
                    }.onFailure { exception ->
                        showToast("❌ Gagal menyimpan gambar: ${exception.message}")
                    }
                } catch (e: Exception) {
                    showToast("❌ Terjadi kesalahan: ${e.message}")
                } finally {
                    Handler(Looper.getMainLooper()).postDelayed({
                        btnSaveImage.isEnabled = true
                        btnSaveImage.text = "Simpan\nGambar"
                    }, 10000) // aktif lagi setelah 5 detik
                }
            }
        } else {
            Thread {
                try {
                    val result = kotlinx.coroutines.runBlocking {
                        fileHelper.saveBitmapToGallery(receiptBitmap!!, transaction.id)
                    }
                    mainHandler.post {
                        result.onSuccess { message ->
                            showToast("✅ $message")
                        }.onFailure { exception ->
                            showToast("❌ Gagal menyimpan gambar: ${exception.message}")
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            btnSaveImage.isEnabled = true
                            btnSaveImage.text = "Simpan\nGambar"
                        }, 10000)
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        showToast("❌ Terjadi kesalahan: ${e.message}")
                        Handler(Looper.getMainLooper()).postDelayed({
                            btnSaveImage.isEnabled = true
                            btnSaveImage.text = "Simpan\nGambar"
                        }, 10000)
                    }
                }
            }.start()
        }
    }

    private fun downloadReceiptImage() {
        if (receiptBitmap == null) {
            showToast("Sedang memproses struk, silakan coba lagi...")
            generateReceiptBitmap()
            return
        }

        btnDownload.isEnabled = false
        btnDownload.text = "Downloading..."

        if (context is androidx.lifecycle.LifecycleOwner) {
            (context as androidx.lifecycle.LifecycleOwner).lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        fileHelper.saveBitmapToDownloads(receiptBitmap!!, transaction.id)
                    }
                    result.onSuccess { message ->
                        showToast("📁 $message")
                    }.onFailure { exception ->
                        showToast("❌ Gagal mendownload gambar: ${exception.message}")
                    }
                } catch (e: Exception) {
                    showToast("❌ Terjadi kesalahan: ${e.message}")
                } finally {
                    Handler(Looper.getMainLooper()).postDelayed({
                        btnDownload.isEnabled = true
                        btnDownload.text = "Download"
                    }, 10000) // aktif lagi setelah 5 detik
                }
            }
        } else {
            Thread {
                try {
                    val result = kotlinx.coroutines.runBlocking {
                        fileHelper.saveBitmapToDownloads(receiptBitmap!!, transaction.id)
                    }
                    mainHandler.post {
                        result.onSuccess { message ->
                            showToast("📁 $message")
                        }.onFailure { exception ->
                            showToast("❌ Gagal mendownload gambar: ${exception.message}")
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            btnDownload.isEnabled = true
                            btnDownload.text = "Download"
                        }, 10000)
                    }
                } catch (e: Exception) {
                    mainHandler.post {
                        showToast("❌ Terjadi kesalahan: ${e.message}")
                        Handler(Looper.getMainLooper()).postDelayed({
                            btnDownload.isEnabled = true
                            btnDownload.text = "Download"
                        }, 10000)
                    }
                }
            }.start()
        }
    }

    private fun printReceipt() {
        if (!printHelper.isPrintingAvailable()) {
            showToast("❌ Fitur print tidak tersedia di perangkat ini")
            return
        }

        if (receiptBitmap == null) {
            showToast("Sedang memproses struk, silakan coba lagi...")
            generateReceiptBitmap()
            return
        }

        try {
            printHelper.printReceipt(transaction, cartItems, paymentAmount, change)
            showToast("🖨️ Membuka dialog print...")
        } catch (e: Exception) {
            showToast("❌ Gagal membuka dialog print: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } else {
            mainHandler.post {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        receiptBitmap?.recycle()
        receiptBitmap = null
    }
}

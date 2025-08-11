package com.example.kasirberas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import com.example.kasirberas.data.model.CartItem
import com.example.kasirberas.data.model.Transaction
import java.io.FileOutputStream
import java.io.IOException

class PrintHelper(private val context: Context) {

    /**
     * Print receipt menggunakan Android Print Framework
     */
    fun printReceipt(
        transaction: Transaction,
        cartItems: List<CartItem>,
        paymentAmount: Double,
        change: Double
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val receiptGenerator = ReceiptGenerator(context)
        val bitmap = receiptGenerator.generateReceiptBitmap(
            transaction, cartItems, paymentAmount, change
        )

        val printAdapter = ReceiptPrintAdapter(
            "Receipt_${transaction.id}",
            bitmap
        )

        printManager.print(
            "Receipt_${transaction.id}",
            printAdapter,
            null
        )
    }

    /**
     * Print bitmap langsung
     */
    fun printBitmap(bitmap: Bitmap, jobName: String = "Receipt") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = ReceiptPrintAdapter(jobName, bitmap)

        printManager.print(jobName, printAdapter, null)
    }

    /**
     * Custom PrintDocumentAdapter untuk print bitmap
     */
    private inner class ReceiptPrintAdapter(
        private val jobName: String,
        private val bitmap: Bitmap
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val pdi = PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()

            callback.onLayoutFinished(pdi, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            try {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    return
                }

                val pdfDocument = PdfDocument()

                val pageInfo = PdfDocument.PageInfo.Builder(
                    bitmap.width,
                    bitmap.height,
                    1
                ).create()

                val page = pdfDocument.startPage(pageInfo)

                // Draw bitmap to PDF
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)

                pdfDocument.finishPage(page)

                // Write PDF to destination
                FileOutputStream(destination.fileDescriptor).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                pdfDocument.close()

                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))

            } catch (e: IOException) {
                callback.onWriteFailed(e.message)
            } catch (e: Exception) {
                callback.onWriteFailed(e.message)
            }
        }

        override fun onFinish() {
            super.onFinish()
            // Cleanup if needed
        }
    }

    /**
     * Check if printing is available
     */
    fun isPrintingAvailable(): Boolean {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        return printManager != null
    }

    /**
     * Get print attributes for receipt
     */
    private fun getReceiptPrintAttributes(): PrintAttributes {
        return PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("receipt", "Receipt", 300, 300))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
    }
}
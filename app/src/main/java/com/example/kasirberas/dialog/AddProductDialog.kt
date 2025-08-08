package com.example.kasirberas.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.kasirberas.R
import com.example.kasirberas.data.model.Product
import com.example.kasirberas.data.model.ProductCategory
import com.example.kasirberas.databinding.DialogAddProductBinding
import com.example.kasirberas.utils.ImageHelper

class AddProductDialog(
    private val onProductAdded: (Product) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageHelper: ImageHelper
    private var selectedImageUri: Uri? = null
    private var savedImagePath: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddProductBinding.inflate(layoutInflater)
        imageHelper = ImageHelper(requireContext())

        setupSpinner()
        setupClickListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Produk Baru")
            .setView(binding.root)
            .setPositiveButton("Simpan") { _, _ ->
                saveProduct()
            }
            .setNegativeButton("Batal", null)
            .create()
    }

    private fun setupSpinner() {
        val categories = ProductCategory.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.buttonSelectImage.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(800, 600)
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                binding.imagePreview.setImageURI(uri)
                binding.imagePreview.visibility = View.VISIBLE
                binding.textImageSelected.visibility = View.VISIBLE
                binding.textImageSelected.text = "Gambar dipilih"
            }
        }
    }

    private fun saveProduct() {
        val name = binding.editTextName.text.toString().trim()
        val priceText = binding.editTextPrice.text.toString().trim()
        val stockText = binding.editTextStock.text.toString().trim()
        val unit = binding.editTextUnit.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val selectedCategoryName = binding.spinnerCategory.text.toString()
        val categoryIndex = ProductCategory.values().indexOfFirst { it.displayName == selectedCategoryName }

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(context, "Nama produk harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceText.isEmpty()) {
            Toast.makeText(context, "Harga harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (stockText.isEmpty()) {
            Toast.makeText(context, "Stok harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (categoryIndex == -1) {
            Toast.makeText(context, "Pilih kategori produk", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceText.toDoubleOrNull() ?: 0.0
        val stock = stockText.toIntOrNull() ?: 0

        if (price <= 0) {
            Toast.makeText(context, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Save image if selected
        selectedImageUri?.let { uri ->
            savedImagePath = imageHelper.saveImageToInternalStorage(uri) ?: ""
        }

        val product = Product(
            name = name,
            price = price,
            category = ProductCategory.values()[categoryIndex],
            unit = unit.ifEmpty { "pcs" },
            stock = stock,
            imagePath = savedImagePath,
            description = description
        )

        onProductAdded(product)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

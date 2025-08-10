package com.example.kasirberas.ui.product

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kasirberas.adapter.ProductAdapter
import com.example.kasirberas.databinding.FragmentProductBinding
import com.example.kasirberas.dialog.AddProductDialog

class ProductFragment : Fragment() {
    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var productViewModel: ProductViewModel
    private lateinit var productAdapter: ProductAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                showProductOptions(product)
            },
            onAddToCartClick = { product ->
                productViewModel.addToCart(product)
            }
        )

        binding.recyclerViewProducts.apply {
            adapter = productAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setupSearchView() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                productViewModel.searchProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupObservers() {
        productViewModel.filteredProducts.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)

            if (products.isEmpty()) {
                binding.recyclerViewProducts.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerViewProducts.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }
        }

        productViewModel.message.observe(viewLifecycleOwner) { message ->
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                productViewModel.clearMessage()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddProduct.setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun showAddProductDialog() {
        val dialog = AddProductDialog { product ->
            productViewModel.addProduct(product)
        }
        dialog.show(childFragmentManager, "AddProductDialog")
    }

    private fun showProductOptions(product: com.example.kasirberas.data.model.Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(product.name)
            .setItems(arrayOf("Edit", "Tambah ke Keranjang", "Hapus")) { _, which ->
                when (which) {
                    0 -> {
                        // TODO: Implement edit functionality
                        Toast.makeText(context, "Edit akan ditambahkan nanti", Toast.LENGTH_SHORT).show()
                    }
                    1 -> productViewModel.addToCart(product)
                    2 -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Hapus Produk")
                            .setMessage("Yakin ingin menghapus ${product.name}?")
                            .setPositiveButton("Ya") { _, _ ->
                                productViewModel.deleteProduct(product)
                            }
                            .setNegativeButton("Tidak", null)
                            .show()
                    }
                }
            }
            .show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

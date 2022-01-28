package com.lentatyka.warehouse.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.database.WarehouseApp
import com.lentatyka.warehouse.databinding.AddItemFragmentBinding
import com.lentatyka.warehouse.utils.FieldType
import com.lentatyka.warehouse.utils.TABLE_COLS
import com.lentatyka.warehouse.viewmodels.WarehouseViewModel
import com.lentatyka.warehouse.viewmodels.WarehouseViewModelFactory
import kotlinx.coroutines.flow.collect

class AddItemFragment : Fragment() {
    private var _binding: AddItemFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WarehouseViewModel by activityViewModels {
        WarehouseViewModelFactory(
            (activity?.application as WarehouseApp).database
        )
    }
    private val navigationArgs: AddItemFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddItemFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemId = navigationArgs.itemId
        if (itemId >= 0) {
            lifecycleScope.launchWhenStarted {
                viewModel.getItem(itemId).collect { item ->
                    bind(item.cols)
                }
            }
            binding.btnSave.setOnClickListener {
                updateItem()
            }
        } else {
            binding.btnSave.setOnClickListener {
                addNewItem()
            }
            bind(null)
        }
    }

    private fun updateItem() {
        val listView = getFieldInfo()
        if (isEntryValid(listView)) {
            viewModel.updateItem(navigationArgs.itemId, listView)
            val action = AddItemFragmentDirections.actionAddItemFragmentToTablesFragment()
            findNavController().navigate(action)
        }
    }

    private fun addNewItem() {
        val listView = getFieldInfo()
        if (isEntryValid(listView)) {
            viewModel.addNewItem(listView)
            val action = AddItemFragmentDirections.actionAddItemFragmentToTablesFragment()
            findNavController().navigate(action)
        }
    }

    private fun getFieldInfo(): ArrayList<String> {
        val container = binding.container
        val listView = arrayListOf<String>()
        for (i in 0 until container.childCount) {
            val view = container.getChildAt(i).findViewById<TextInputEditText>(R.id.add_item_edit)
            listView += view.text.toString()
        }
        return listView
    }

    private fun bind(items: ArrayList<String>?) {
        val container = binding.container
        for (i in TABLE_COLS.indices) {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.add_item, null)
            val textInputLayout = view.findViewById<TextInputLayout>(R.id.item_label)
            textInputLayout.hint = TABLE_COLS[i].name
            val textInputEditText = view.findViewById<TextInputEditText>(R.id.add_item_edit)
            when (TABLE_COLS[i].type) {
                FieldType.INTEGER ->
                    textInputEditText.inputType = (InputType.TYPE_CLASS_NUMBER
                            + InputType.TYPE_NUMBER_FLAG_SIGNED)
                FieldType.TEXT ->
                    textInputEditText.inputType = InputType.TYPE_CLASS_TEXT
                FieldType.REAL ->
                    textInputEditText.inputType = (InputType.TYPE_CLASS_NUMBER
                            + InputType.TYPE_NUMBER_FLAG_DECIMAL
                            + InputType.TYPE_NUMBER_FLAG_SIGNED)
            }
            textInputEditText.setText(items?.let {
                items[i]
            } ?: "")
            container.addView(view)
        }
    }

    private fun isEntryValid(array: ArrayList<String>): Boolean {
        return viewModel.isEntryValid(array)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
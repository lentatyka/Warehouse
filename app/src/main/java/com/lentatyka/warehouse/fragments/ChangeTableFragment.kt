package com.lentatyka.warehouse.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.adapters.ChangeTableAdapter
import com.lentatyka.warehouse.database.WarehouseApp
import com.lentatyka.warehouse.databinding.FragmentChangeTableBinding
import com.lentatyka.warehouse.utils.MAX_TABLES
import com.lentatyka.warehouse.viewmodels.WarehouseViewModel
import com.lentatyka.warehouse.viewmodels.WarehouseViewModelFactory
import kotlinx.coroutines.flow.collect

class ChangeTableFragment : Fragment() {
    private val viewModel: WarehouseViewModel by activityViewModels{
        WarehouseViewModelFactory(
            (activity?.application as WarehouseApp).database
        )
    }
    private var _binding: FragmentChangeTableBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangeTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ChangeTableAdapter(
            //Change table
            {
                viewModel.setTable(it)
                val action =
                    ChangeTableFragmentDirections.actionChangeTableFragmentToTablesFragment()
                findNavController().navigate(action)
            })
        //Remove changed table
        {
            confirmRemove(it)
        }
        binding.btnAddTable.setOnClickListener {
            val action =
                ChangeTableFragmentDirections.actionChangeTableFragmentToCreateTableFragment(
                    adapter.currentList.toTypedArray()
                )
            findNavController().navigate(action)
        }
        binding.tableNameRecycler.adapter = adapter
        viewModel.updateTableList()
        lifecycleScope.launchWhenStarted {
            viewModel.tableNames.collect{
                adapter.submitList(it)
                binding.btnAddTable.visibility = if (it.size < MAX_TABLES) View.VISIBLE else View.GONE
            }
        }
    }
    private fun confirmRemove(name: String) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder
            .setTitle(getString(R.string.remove_table, name))
            .setPositiveButton(R.string.yes_btn) { _, _ ->
                viewModel.deleteTable(name)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        builder.create()
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}
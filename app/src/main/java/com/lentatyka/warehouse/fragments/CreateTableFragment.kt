package com.lentatyka.warehouse.fragments


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.adapters.CreateTableAdapter
import com.lentatyka.warehouse.database.WarehouseApp
import com.lentatyka.warehouse.databinding.FragmentCreateTableBinding
import com.lentatyka.warehouse.utils.CreateColumnDialog
import com.lentatyka.warehouse.utils.FieldCode
import com.lentatyka.warehouse.utils.MAX_TABLE_COLS
import com.lentatyka.warehouse.viewmodels.CreateTableViewModel
import com.lentatyka.warehouse.viewmodels.WarehouseViewModelFactory
import kotlinx.coroutines.flow.collect


class CreateTableFragment : Fragment() {
    private val viewModel: CreateTableViewModel by viewModels {
        WarehouseViewModelFactory(
            (activity?.application as WarehouseApp).database
        )
    }
    private var _binding: FragmentCreateTableBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: CreateTableFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CreateTableAdapter({
            //Edit current field by index
            CreateColumnDialog(requireContext(), viewModel.columnNames.value!![it], it,
                { name, isNew ->
                    viewModel.isValidField(name, isNew)
                }) { name, type, index ->
                viewModel.addColumn(name, type, index)
            }.show()
        }) {
            //Remove current field by index
            confirmRemove(it)
        }
        val itemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
        binding.tableEditRecycler.addItemDecoration(itemDecoration)
        binding.tableEditRecycler.adapter = adapter
        lifecycleScope.launchWhenStarted {
            viewModel.columnNames.collect {list->
                list?.let { adapter.submitList(it) }
            }
        }
        binding.btnAddCol.setOnClickListener {
            if (!isMaxColumns()) {
                CreateColumnDialog(requireContext(), null, null,
                    { name, isNew ->
                        viewModel.isValidField(name, isNew)
                    }) { name, type, index ->
                    viewModel.addColumn(name, type, index)
                }.show()
            } else {
                showSnackBar("Максимум $MAX_TABLE_COLS полей")
            }
        }
    }

    private fun isMaxColumns() = viewModel.isMaxColumns()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_save_menu -> {
                val tableName = binding.tableNameEdit.text.toString()
                val answer = viewModel.saveTable(tableName, navigationArgs.tabNames)
                if (answer != FieldCode.SUCCESS) {
                    val message = when (answer) {
                        FieldCode.EMPTY -> R.string.empty_name
                        FieldCode.BAD_NAME -> R.string.exists_name
                        else -> R.string.unaccabtable
                    }
                    showSnackBar(getString(message))
                } else {
                    val action =
                        CreateTableFragmentDirections.actionCreateTableFragmentToChangeTableFragment()
                    findNavController().navigate(action)
                }
            }
            else -> findNavController().navigateUp()
        }
        return true
    }

    private fun confirmRemove(index: Int) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder
            .setTitle("Remove field \"${viewModel.getColumnName(index)}\"")
            .setPositiveButton(R.string.yes_btn) { _, _ ->
                viewModel.deleteColumn(index)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        builder.create()
        builder.show()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok_btn) {}
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.lentatyka.warehouse.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.adapters.TableViewAdapter
import com.lentatyka.warehouse.database.WarehouseApp
import com.lentatyka.warehouse.databinding.TableViewFragmentBinding
import com.lentatyka.warehouse.viewmodels.WarehouseViewModel
import com.lentatyka.warehouse.viewmodels.WarehouseViewModelFactory
import kotlinx.coroutines.flow.collect

class TableViewFragment : Fragment() {

    private var _binding: TableViewFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter:TableViewAdapter

    private val viewModel: WarehouseViewModel  by activityViewModels{
        WarehouseViewModelFactory(
            (activity?.application as WarehouseApp).database
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TableViewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemDecoration = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
        binding.tableviewRecycler.addItemDecoration(itemDecoration)
        adapter = TableViewAdapter({
            val action = TableViewFragmentDirections.actionTablesFragmentToAddItemFragment(it)
            findNavController().navigate(action)
        }){viewModel.deleteItem(it)}
        binding.tableviewRecycler.adapter = adapter
        lifecycleScope.launchWhenStarted {
            viewModel.allItems.collect {list->
                list?.let {
                    adapter.setList(it)}
            }
        }
        binding.btnAddItem.setOnClickListener {
            val action = TableViewFragmentDirections.actionTablesFragmentToAddItemFragment()
            findNavController().navigate(action)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextChange(query: String?): Boolean {
                    query?.let {
                        adapter.filter.filter(query)}
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
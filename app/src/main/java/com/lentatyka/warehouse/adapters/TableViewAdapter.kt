package com.lentatyka.warehouse.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.database.TableRows
import com.lentatyka.warehouse.databinding.TableItemBinding
import com.lentatyka.warehouse.utils.TABLE_COLS

class TableViewAdapter(
    private val editClick: (Int) -> Unit,
    private val deleteClick: (TableRows) -> Unit
) :
    ListAdapter<TableRows, TableViewAdapter.TableViewHolder>(DiffCallback), Filterable {
    private lateinit var fullList: List<TableRows>

    class TableViewHolder(private var binding: TableItemBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TableRows, onEdit: (Int) -> Unit, onDelete: (TableRows) -> Unit) {
            binding.tableContainer.removeAllViews()
            val primaryView = LayoutInflater.from(context).inflate(R.layout.table_item_primary, null)
            (primaryView.findViewById(R.id.name0_label) as TextView).apply { text = TABLE_COLS[0].name }
            (primaryView.findViewById(R.id.name0) as TextView).apply { text = item.cols[0] }
            binding.tableContainer.addView(primaryView)
            for (col in 1 until item.cols.size) {
                val view = LayoutInflater.from(context).inflate(R.layout.table_item_secondary, null)
                (view.findViewById(R.id.name1_label) as TextView).apply {
                    text = TABLE_COLS[col].name
                }
                (view.findViewById(R.id.name1) as TextView).apply { text = item.cols[col] }
                binding.tableContainer.addView(view)
            }
            binding.btnEdit.setOnClickListener {
                onEdit(item.id)
            }
            binding.btnDelete.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        return TableViewHolder(
            TableItemBinding.inflate(LayoutInflater.from(parent.context)),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, { editClick(it) }) { deleteClick(it) }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TableRows>() {
            override fun areContentsTheSame(oldItem: TableRows, newItem: TableRows): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areItemsTheSame(oldItem: TableRows, newItem: TableRows): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun setList(list: List<TableRows>) {
        fullList = list
        submitList(list)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(query: CharSequence?): FilterResults {
                val filteredList = mutableListOf<TableRows>()
                if (query.isNullOrEmpty()) {
                    filteredList.addAll(fullList)
                } else {
                    val pattern = query.toString().trim()
                    fullList.forEach { tr ->
                        for (i in tr.cols.indices) {
                            if (tr.cols[i].contains(pattern, true)) {
                                filteredList.add(tr)
                                break
                            }
                        }
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(query: CharSequence?, results: FilterResults?) {
                submitList(results!!.values as List<TableRows>)
            }
        }
    }
}
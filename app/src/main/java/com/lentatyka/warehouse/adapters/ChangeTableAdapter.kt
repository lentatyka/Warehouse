package com.lentatyka.warehouse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lentatyka.warehouse.databinding.TablesNameItemBinding

class ChangeTableAdapter(
    private val changeTable: (String) -> Unit,
    private val deleteTable: (String) -> Unit
) :
    ListAdapter<String, ChangeTableAdapter.TableViewHolder>(DiffCallback) {

    class TableViewHolder(private val binding: TablesNameItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, onChange: (String)->Unit, onDelete: (String)->Unit){
            binding.btnName.text = item
            binding.btnName.setOnClickListener { onChange(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        return TableViewHolder(TablesNameItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, {changeTable(it)}){deleteTable(it)}
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}
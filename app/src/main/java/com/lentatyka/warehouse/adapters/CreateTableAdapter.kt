package com.lentatyka.warehouse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lentatyka.warehouse.databinding.TableEditItemBinding
import com.lentatyka.warehouse.utils.TableCol

class CreateTableAdapter(private val editClick: (Int)->Unit, private val removeClick: (Int)->Unit):
    ListAdapter<TableCol, CreateTableAdapter.TableHolder>(DiffCallback){

    class TableHolder(private var binding: TableEditItemBinding):
        RecyclerView.ViewHolder(binding.root){
        fun bind(item: TableCol, onEdit: (Int)->Unit, onRemove: (Int)->Unit){
            binding.fieldNameText.text = item.name
            binding.fieldTypeText.text = item.type.toString()
            binding.btnEditField.setOnClickListener { onEdit(adapterPosition) }
            binding.btnRemoveField.setOnClickListener { onRemove(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableHolder {
        return TableHolder(TableEditItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TableHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, {editClick(it)}){removeClick(it)}
    }

    companion object{
        private val DiffCallback = object : DiffUtil.ItemCallback<TableCol>(){
            override fun areContentsTheSame(oldItem: TableCol, newItem: TableCol): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areItemsTheSame(oldItem: TableCol, newItem: TableCol): Boolean {
                return oldItem == newItem
            }
        }
    }
}
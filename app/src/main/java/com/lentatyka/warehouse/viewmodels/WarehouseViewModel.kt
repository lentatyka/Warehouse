package com.lentatyka.warehouse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lentatyka.warehouse.database.DatabaseHandler
import com.lentatyka.warehouse.database.TableRows
import com.lentatyka.warehouse.utils.TABLE_COLS
import com.lentatyka.warehouse.utils.TABLE_NAME
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WarehouseViewModel(private val database: DatabaseHandler) : ViewModel(){
    private val fullList = mutableListOf<TableRows>()
    private val _allItems = MutableSharedFlow<List<TableRows>?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).apply { tryEmit(null) }
    val allItems: SharedFlow<List<TableRows>?> = _allItems.asSharedFlow()

    fun getItem(itemId: Int): Flow<TableRows> = flow {
        emit(database.getItemById(itemId))
    }

    fun isEntryValid(array: ArrayList<String>): Boolean {
        array.forEach {
            if (it.isBlank())
                return false
        }
        return true
    }

    fun addNewItem(listItem: ArrayList<String>) {
        val tableRows = TableRows(cols = listItem)
        viewModelScope.launch {
            database.insertItem(tableRows)
            _allItems.emit(database.getAllItems())
        }
    }

    fun updateItem(itemId: Int, listItem: ArrayList<String>) {
        val tableRows = TableRows(itemId, listItem)
        viewModelScope.launch {
            database.updateItem(tableRows)
            _allItems.emit(database.getAllItems())
        }
    }

    fun deleteItem(item: TableRows) {
        viewModelScope.launch {
            if (database.deleteItem(item.id) > 0) {
                _allItems.emit(
                    _allItems.first()?.minus(item)
                )
            }
        }
    }

    private var _tableNames = MutableStateFlow<List<String>>(database.getTableNames())
    val tableNames: StateFlow<List<String>> get() = _tableNames.asStateFlow()

    fun deleteTable(name: String) {
        _tableNames.value = _tableNames.value.minus(name)
        database.deleteTable(name)
    }

    fun updateTableList(){
        _tableNames.value = database.getTableNames()
    }
    fun setTable(tableName: String) {
        TABLE_NAME = tableName
        viewModelScope.launch {
            TABLE_COLS = database.getTableCols()
            fullList.clear()
            fullList.addAll(database.getAllItems())
            _allItems.emit(fullList)
        }
    }
}

class WarehouseViewModelFactory(private val database: DatabaseHandler) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(WarehouseViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return WarehouseViewModel(database) as T
            }
            modelClass.isAssignableFrom(CreateTableViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return CreateTableViewModel(database) as T
            }
        }
        throw IllegalArgumentException("Unknown viewmodel")
    }
}
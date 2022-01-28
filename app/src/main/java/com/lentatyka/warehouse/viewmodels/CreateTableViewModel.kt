package com.lentatyka.warehouse.viewmodels
import androidx.lifecycle.ViewModel
import com.lentatyka.warehouse.database.DatabaseHandler
import com.lentatyka.warehouse.utils.FieldCode
import com.lentatyka.warehouse.utils.FieldType
import com.lentatyka.warehouse.utils.MAX_TABLE_COLS
import com.lentatyka.warehouse.utils.TableCol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreateTableViewModel(private val database: DatabaseHandler) : ViewModel() {
    private val regex = Regex("^[a-zа-я0-9_]+$", RegexOption.IGNORE_CASE)
    private var _columnNames = MutableStateFlow<List<TableCol>>(listOf())
    val columnNames: StateFlow<List<TableCol>> get() = _columnNames.asStateFlow()

    private fun getNewEntry(name: String, type: FieldType) = TableCol(name, type)

    fun addColumn(name: String, type: FieldType, index: Int?) {
        _columnNames.value =
            if (index == null){
                columnNames.value.plus(getNewEntry(name, type))
            }
            else {
                _columnNames.value.let {
                    val list = it.toMutableList()
                    list[index] = getNewEntry(name, type)
                    list
                }
            }
    }

    fun deleteColumn(index: Int) {
        _columnNames.value = _columnNames.value.let {
            val list = it.toMutableList()
            list.removeAt(index)
            list
        }
    }

    fun isMaxColumns() = _columnNames.value.size >= MAX_TABLE_COLS

    fun getColumnName(index: Int) = _columnNames.value!![index].name

    fun saveTable(tableName: String, tableArray: Array<String>?): FieldCode {
        //Empty fields
        if (tableName.isEmpty() || _columnNames.value.isEmpty())
            return FieldCode.EMPTY
        //Only words and numbers
        else if (!regex.matches(tableName))
            return FieldCode.BAD_SYMBOLS
        //Get preferences and check exists name
        val tabName = tableName.lowercase()
        if(tableArray?.contains(tabName) == true)
            return FieldCode.BAD_NAME
        //Name is valid. Create table
        database.createTable(tabName, _columnNames.value.toTypedArray())
        return FieldCode.SUCCESS
    }

    fun isValidField(name: String, isNew: Boolean): FieldCode {
        if (name.isEmpty()) {
            return FieldCode.EMPTY
        } else if (!regex.matches(name)) {
            return FieldCode.BAD_SYMBOLS
        } else if (isNew)
            _columnNames.value.forEach {
                if (it.name.equals(name, true)) return FieldCode.BAD_NAME
            }
        return FieldCode.SUCCESS
    }
}
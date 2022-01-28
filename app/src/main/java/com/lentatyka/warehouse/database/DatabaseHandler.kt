package com.lentatyka.warehouse.database

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.lentatyka.warehouse.utils.FieldType
import com.lentatyka.warehouse.utils.TABLE_COLS
import com.lentatyka.warehouse.utils.TABLE_NAME
import com.lentatyka.warehouse.utils.TableCol

private const val TABLES_BASE = "table_base"

class DatabaseHandler private constructor(context: Context) : SQLiteOpenHelper(
    context, "tester", null, 1
) {
    override fun onCreate(db: SQLiteDatabase?) {
        //Init table with name of tables
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLES_BASE (NAME TEXT PRIMARY KEY)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLES_BASE")
        onCreate(db)
    }


    @SuppressLint("Recycle")
    fun getTableNames(): ArrayList<String> {
        val list = arrayListOf<String>()
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery("SELECT NAME FROM $TABLES_BASE", null)
        } catch (e: SQLiteException) {
            return list
        }
        cursor?.let {
            if (it.moveToFirst()) {
                do {
                    list += it.getString(it.getColumnIndex("NAME"))
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun insertItem(data: TableRows): Long {
        val db = this.writableDatabase
        val success: Long
        try {
            val contentValues = contentValuesOf()
            for (i in TABLE_COLS.indices) {
                contentValues.put("_${TABLE_COLS[i].name}", data.cols[i])
            }
            success = db.insert("_$TABLE_NAME", null, contentValues)
        } catch (e: SQLiteException) {
            return -1
        } finally {
            db.close()
        }
        return success
    }

    @SuppressLint("Recycle")
    private fun requestItems(query: String, args: Array<String>?): ArrayList<TableRows> {
        val db = this.readableDatabase
        val cursor: Cursor?
        return try {
            cursor = db.rawQuery(query, args)
            val list = arrayListOf<TableRows>()
            cursor?.let { c ->
                if (c.moveToFirst()) {
                    do {
                        val fieldArray = arrayListOf<String>()
                        for (i in TABLE_COLS.indices) {
                            fieldArray += c.getString(c.getColumnIndex("_${TABLE_COLS[i].name}"))
                        }
                        list.add(TableRows(c.getInt(c.getColumnIndex("id")), fieldArray))
                    } while (c.moveToNext())
                }
            }
            list
        } catch (e: SQLiteException) {
            arrayListOf()
        }
    }

    private fun initCreateTable(tableName: String, tableCols: Array<TableCol>): String {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            append("CREATE TABLE IF NOT EXISTS _$tableName (id INTEGER PRIMARY KEY AUTOINCREMENT")
            var name: String
            var type: String
            tableCols.forEach {
                name = it.name
                type = it.type.toString()
                append(", _$name $type")
            }
            append(")")
        }
        return stringBuilder.toString()
    }

    fun createTable(tableName: String, tableCols: Array<TableCol>) {
        val db = this.readableDatabase
        db.execSQL(initCreateTable(tableName, tableCols))
        saveTableData(tableName)
    }

    private fun saveTableData(tableName: String): Long {
        val db = this.writableDatabase
        val success: Long
        try {
            val contentValues = contentValuesOf()
            contentValues.put("NAME", tableName)
            success = db.insert(TABLES_BASE, null, contentValues)
        } catch (e: SQLiteException) {
            return -1
        } finally {
            db.close()
        }
        return success
    }

    fun updateItem(tableRows: TableRows): Int {
        val db = this.writableDatabase
        val success: Int
        try {
            val contentValues = contentValuesOf()
            for (i in tableRows.cols.indices) {
                contentValues.put("_${TABLE_COLS[i].name}", tableRows.cols[i])
            }
            success =
                db.update("_$TABLE_NAME", contentValues, "id = ?", arrayOf(tableRows.id.toString()))
        } catch (e: SQLiteException) {
            return -1
        } finally {
            db.close()
        }
        return success
    }

    fun deleteItem(itemId: Int): Int {
        val db = this.writableDatabase
        val success: Int
        try {
            success = db.delete("_$TABLE_NAME", "id = $itemId", null)
        } catch (e: SQLiteException) {
            return -1
        } finally {
            db.close()
        }
        return success
    }

    fun deleteTable(name: String) {
        fun deleteTableFromBase(name: String) {
            val db = this.writableDatabase
            try {
                db.delete(TABLES_BASE, "NAME = ?", arrayOf(name))
            } catch (e: SQLiteException) {
                e.printStackTrace()
            } finally {
                db.close()
            }
        }

        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS _$name")
        deleteTableFromBase(name)
    }

    fun getTableCols(): Array<TableCol> {
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.rawQuery("PRAGMA table_info(_$TABLE_NAME)", null)
        } catch (e: SQLiteException) {
            return arrayOf()
        }
        return cursor?.let {
            val list = arrayListOf<TableCol>()
            if (it.moveToFirst()) {
                //skip id
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndex("name")).substring(1)
                    val type = when (it.getString(it.getColumnIndex("type"))) {
                        "INTEGER" -> FieldType.INTEGER
                        "REAL" -> FieldType.REAL
                        else -> FieldType.TEXT
                    }
                    list += TableCol(name, type)
                }
            }
            list.toTypedArray()
        } ?: arrayOf()
    }

    fun getAllItems(): ArrayList<TableRows>{
        return (requestItems("SELECT * FROM _${TABLE_NAME}", null))
    }

    fun getItemById(itemId: Int):TableRows{
        val list = requestItems("SELECT * FROM _$TABLE_NAME WHERE id = ?", arrayOf(itemId.toString()))
        return list[0]
    }

    fun searchItem(param: String): List<TableRows> {
        val query = StringBuilder("SELECT * FROM _$TABLE_NAME WHERE ")
        with(query){
            for(i in TABLE_COLS.indices){
                append("_${TABLE_COLS[i].name} LIKE '%$param%'")
                if(i < TABLE_COLS.size-1)
                    append(" OR ")
            }
        }
        return requestItems(query.toString(), null)
    }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHandler? = null

        fun getDatabase(context: Context): DatabaseHandler {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHandler(context)
                INSTANCE = instance
                return instance
            }
        }
    }
}
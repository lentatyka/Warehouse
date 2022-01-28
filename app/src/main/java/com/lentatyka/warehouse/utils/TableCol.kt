package com.lentatyka.warehouse.utils

data class TableCol(val name: String, var type: FieldType)

enum class FieldType{
    TEXT, INTEGER, REAL
}
enum class FieldCode{
    BAD_NAME, EMPTY, SUCCESS, BAD_SYMBOLS
}
package com.lentatyka.warehouse.database

import android.app.Application

class WarehouseApp: Application() {
    val database: DatabaseHandler by lazy { DatabaseHandler.getDatabase(this)}
}
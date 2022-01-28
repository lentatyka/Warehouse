package com.lentatyka.warehouse.viewmodels

import com.google.common.truth.Truth.assertThat
import com.lentatyka.warehouse.utils.FieldCode
import org.junit.Test

class WarehouseViewModelTest{

    fun isEntryValid(array: ArrayList<String>): Boolean {
        array.forEach {
            if (it.isBlank())
                return false
        }
        return true
    }

    @Test
    fun `array has empty return false`(){
        val result = isEntryValid(arrayListOf("d"))
        assertThat(result).isFalse()
    }
}
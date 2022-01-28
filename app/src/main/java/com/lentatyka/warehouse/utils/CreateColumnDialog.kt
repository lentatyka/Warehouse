package com.lentatyka.warehouse.utils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.lentatyka.warehouse.R
import com.lentatyka.warehouse.databinding.DialogCreateFieldBinding

class CreateColumnDialog(
    context: Context,
    private val tableField: TableCol?,
    private val index: Int?,
    private val checkField: (String, Boolean) -> FieldCode,
    private val saveField: (String, FieldType, Int?) -> Unit
) :
    AlertDialog(context) {
    private var _binding: DialogCreateFieldBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = DialogCreateFieldBinding.inflate(LayoutInflater.from(context))
        setView(binding.root)
        if(tableField == null)
            setTitle("Создать")
        else{
            setTitle("Править")
            binding.fieldNameEdit.setText(tableField.name)
        }
        binding.btnOk.setOnClickListener {
            val answer = checkField(binding.fieldNameEdit.text.toString(),
                index == null
            )
            if (answer == FieldCode.SUCCESS) {
                val fieldType = when (binding.radioGroup.checkedRadioButtonId) {
                    R.id.btn_radio_int -> FieldType.INTEGER
                    R.id.btn_radio_real -> FieldType.REAL
                    else -> FieldType.TEXT
                }
                val fieldName = binding.fieldNameEdit.text.toString()
                saveField(fieldName, fieldType, index)
                dismiss()
            } else {
                binding.fieldNameEdit.error = when (answer) {
                    FieldCode.EMPTY -> "Пустое поле"
                    FieldCode.BAD_SYMBOLS -> "Недопустимые символы"
                    else -> "Имя уже используется"
                }
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onDetachedFromWindow() {
        _binding = null
        super.onDetachedFromWindow()
    }

}
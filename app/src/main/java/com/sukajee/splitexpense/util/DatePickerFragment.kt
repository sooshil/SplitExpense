package com.sukajee.splitexpense.util

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.sukajee.splitexpense.R
import kotlinx.android.synthetic.main.fragment_add_new_transaction.*
import java.text.DateFormat
import java.util.*

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(requireActivity(), this, year, month, dayOfMonth)
            dialog.datePicker.maxDate = System.currentTimeMillis()
            return dialog
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, day)
            val currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
            val transactiondateMillis = c.timeInMillis
            parentFragment?.view?.findViewById<Button>(R.id.buttonTransactionDateInMillis)?.text = transactiondateMillis.toString()
            parentFragment?.view?.findViewById<Button>(R.id.buttonTransactionDate)?.text = currentDate
            Log.d("DATE_PICKER","DateFormatted: $currentDate")
            Log.d("DATE_PICKER","DateInMillis: $transactiondateMillis")
        }
    }


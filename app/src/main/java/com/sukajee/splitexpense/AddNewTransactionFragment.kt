package com.sukajee.splitexpense

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sukajee.splitexpense.data.Transaction
import com.sukajee.splitexpense.util.DatePickerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_new_transaction.*
import java.text.DateFormat
import java.util.*

class AddNewTransactionFragment : Fragment(R.layout.fragment_add_new_transaction) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userReference: DatabaseReference
    private lateinit var transactionReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var lastClickTime: Long = 0
    private var circleCode: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userReference = database.reference.child("users")
        transactionReference = database.reference.child("transactions")

        buttonTransactionDate.text = currentDate()
        Log.d("DATE_DEFAULT", "DateFormatted: ${currentDate()}")
        if (buttonTransactionDateInMillis.text.isEmpty()) {
            buttonTransactionDateInMillis.text = System.currentTimeMillis().toString()
            Log.d("DATE_DEFAULT", "DateInMillis: ${System.currentTimeMillis()}")
        }

        buttonSubmit.setOnClickListener {

            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            if (editTextDescription.text.isBlank()) {
                editTextDescription.setError("Description can't be blank.")
                editTextDescription.requestFocus()
                return@setOnClickListener
            }
            if (editTextStore.text.isBlank()) {
                editTextStore.setError("Store name can't be blank.")
                editTextStore.requestFocus()
                return@setOnClickListener
            }
            if (editTextAmount.text.isBlank()) {
                editTextAmount.setError("Amount can't be blank.")
                editTextAmount.requestFocus()
                return@setOnClickListener
            }
            if (editTextNote.text.isBlank()) {
                editTextNote.setError("Note/Remarks can't be blank.")
                editTextNote.requestFocus()
                return@setOnClickListener
            }

            val date = buttonTransactionDateInMillis.text.toString().trim().toLong()
            val description = editTextDescription.text.toString().trim()
            val storeName = editTextStore.text.toString().trim()
            val amount = editTextAmount.text.toString().trim().toFloat()
            val note = editTextNote.text.toString().trim()

            val currentMilli = System.currentTimeMillis()
            val user = firebaseAuth.currentUser
            if (user != null) {
                userReference.child(user.uid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                circleCode = snapshot.child("circleCode").value.toString()
                                val transaction = Transaction(user.uid, currentMilli, date, circleCode, description, storeName, amount, note)
                                val ref = transactionReference.child(date.toString())
                                ref.setValue(transaction)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })

            }
            clearFields()
            findNavController().navigate(R.id.profileFragment)
        }

        buttonTransactionDate.setOnClickListener { v ->
            if (SystemClock.elapsedRealtime() - lastClickTime < 5000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            val newFragment = DatePickerFragment()
            newFragment.show(childFragmentManager, "datePicker")
        }

        buttonClear.setOnClickListener {
            clearFields()
        }
    }

    private fun currentDate(): String {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, c.get(Calendar.YEAR))
        c.set(Calendar.MONTH, c.get(Calendar.MONTH))
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH))
        return DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
    }

    private fun clearFields() {
        editTextDescription.text = null
        editTextStore.text = null
        editTextAmount.text = null
        editTextNote.text = null
    }
}
package com.sukajee.splitexpense

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sukajee.splitexpense.data.Transaction
import com.sukajee.splitexpense.data.User
import com.sukajee.splitexpense.util.DatePickerFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_new_transaction.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.text.DateFormat
import java.util.*

class AddNewTransactionFragment : Fragment(R.layout.fragment_add_new_transaction) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private var lastClickTime: Long = 0
    private var circleCode: String = ""

    companion object {
        private val TAG = "AddNewTransactionFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

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
            var isNavigateReady = false
            if (user != null) {
                val userRef = fireStore.collection("users").document(user.uid)
                progressBarCircular.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    userRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                val serverUser = documentSnapshot.toObject<User>()
                                circleCode = serverUser!!.circleCode
                                val lastSettlementDate = getDate(serverUser.lastSettlementDate.toLong())
                                //check if user is trying to enter data on the date before the last settlement date
                                if(date < serverUser.lastSettlementDate.toLong()) {
                                    Toast.makeText(requireContext(), "You cannot select date before $lastSettlementDate", Toast.LENGTH_LONG).show();
                                    return@addOnSuccessListener
                                } else {
                                    val transaction = Transaction(user.uid, currentMilli, date, circleCode, description, storeName, amount, note)
                                    fireStore.collection("transactions").document(date.toString()).set(transaction)

                                    fireStore.runTransaction { readWriteTransaction ->
                                        val snapshot = readWriteTransaction.get(userRef)
                                        val newTotalContribution = snapshot.getDouble("totalContribution").toString().toFloat() + amount
                                        val newTotalLifeTimeContribution = snapshot.getDouble("totalLifeTimeContribution").toString().toFloat() + amount
                                        readWriteTransaction.update(userRef, "totalContribution", newTotalContribution)
                                        readWriteTransaction.update(userRef, "totalLifeTimeContribution", newTotalLifeTimeContribution)
                                        null
                                    }.addOnSuccessListener {
                                        Log.d(TAG, "Transaction success!")
                                    }.addOnFailureListener { e ->
                                        Log.w(TAG, "Transaction failure.", e)
                                    }

                                    fireStore.collection("users")
                                            .whereEqualTo("circleCode", circleCode)
                                            .get()
                                            .addOnSuccessListener { querySnaps ->
                                                if (!querySnaps.isEmpty) {
                                                    for (document in querySnaps.documents) {
                                                        val oldCircleTotal = document.get("circleTotal")
                                                        val newCircleTotal = oldCircleTotal.toString().toFloat() + amount
                                                        val docRef = fireStore.collection("users").document(document.id)
                                                        fireStore.runBatch { batch ->
                                                            batch.update(docRef, "circleTotal", newCircleTotal.toString().toFloat())
                                                        }
                                                    }
                                                }
                                            }
                                    clearFields()
                                    findNavController().navigate(R.id.profileFragment)
                                }

                            }
                            .addOnFailureListener {
                                Log.e(TAG, "onViewCreated: Error fetching firestore data.")
                            }
                    progressBarCircular.visibility = View.INVISIBLE
                }
            }

        }

        buttonTransactionDate.setOnClickListener { v ->
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

//            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
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
        editTextDescription.requestFocus()
    }

    fun getDate(milliSeconds: Long): String? {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
    }
}
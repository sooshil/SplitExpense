package com.sukajee.splitexpense.ui.details

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.Transaction
import com.sukajee.splitexpense.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class UsersContributionDetailsFragment : Fragment(R.layout.fragment_user_contribution_detail) {

    private lateinit var recyclerContributionDetails: RecyclerView
    private lateinit var progressContributionDetails: ProgressBar
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var textViewAsOfDateTime: TextView
    private lateinit var textViewUsersContributionDetails: TextView

    companion object {
        private const val TAG = "UsersContributionDetail"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerContributionDetails = view.findViewById(R.id.recyclerContributionDetails)
        progressContributionDetails = view.findViewById(R.id.progressContributionDetails)
        textViewAsOfDateTime = view.findViewById(R.id.textViewAsOfDateTime)
        textViewUsersContributionDetails = view.findViewById(R.id.textViewUsersContributionDetails)
        fireStore = FirebaseFirestore.getInstance()

        val args: UsersContributionDetailsFragmentArgs by navArgs()
        val userId = args.userId
        progressContributionDetails.visibility = View.VISIBLE
        textViewAsOfDateTime.text = "As of ${currentDateTime()}"

        CoroutineScope(Dispatchers.Main).launch {
            var lastSettlementDate: Long
            val userRef = fireStore.collection("users").document(userId)
            userRef.get()
                    .addOnSuccessListener {
                        val user = it.toObject<User>()
                        val userName = user!!.firstName
                        lastSettlementDate = user.lastSettlementDate.toLong()
                        if (userId == FirebaseAuth.getInstance().currentUser!!.uid) {
                            textViewUsersContributionDetails.text = "Your Contribution Details:"
                        } else {
                            textViewUsersContributionDetails.text = "$userName's Contribution Details:"
                        }


                        val transactionRef = fireStore.collection("transactions")
                        val transactionList = mutableListOf<Transaction>()
                        transactionRef
                                .whereEqualTo("userId", userId)
                                .whereGreaterThan("dateOfTransaction", lastSettlementDate)
                                .get()
                                .addOnSuccessListener { query ->
                                    if (!query.isEmpty) {
                                        for (transaction in query.documents) {
                                            val singleTransaction = transaction.toObject<Transaction>()
                                            if (singleTransaction != null) {
                                                transactionList.add(singleTransaction)
                                            }
                                        }
                                    }
                                    recyclerContributionDetails.adapter = ContributionDetailsAdapter(transactionList)
                                    recyclerContributionDetails.layoutManager = LinearLayoutManager(context)
                                    recyclerContributionDetails.setHasFixedSize(true)
                                    progressContributionDetails.visibility = View.INVISIBLE
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "onViewCreated: Inside OnFailureListener ${it.message}")
                                }
                    }
        }
    }

    fun currentDateTime(): String {
        val dateFormat: DateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy HH:mm:ss", Locale.getDefault())
        val c = Calendar.getInstance()
        return dateFormat.format(c.time)
    }
}
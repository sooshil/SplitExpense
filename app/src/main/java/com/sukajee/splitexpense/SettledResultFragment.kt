package com.sukajee.splitexpense

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.sukajee.splitexpense.data.SettledResultModel
import com.sukajee.splitexpense.data.Settlement
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.fragment_old_settlement_detail.*
import kotlinx.android.synthetic.main.fragment_settled_result.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SettledResultFragment : Fragment(R.layout.fragment_settled_result) {

    private var lastClickTime: Long = 0
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var circleCode: String
    private lateinit var tickerViewAverageExpense: TickerView
    private lateinit var recyclerViewSettledResult: RecyclerView
    private var isCircleHead by Delegates.notNull<Boolean>()
    private lateinit var typeface: Typeface
    val settleList = mutableListOf<SettledResultModel>()

    companion object {
        private const val TAG = "SettledResultFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settled_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fireStore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        recyclerViewSettledResult = view.findViewById(R.id.recyclerSettlesDetail)
        tickerViewAverageExpense = view.findViewById(R.id.tickerViewAverageExpense)
        tickerViewAverageExpense.setCharacterLists(TickerUtils.provideNumberList())
        typeface = ResourcesCompat.getFont(requireContext(), R.font.bai_jamjuree_medium)!!
        tickerViewAverageExpense.typeface = typeface

        val args: SettledResultFragmentArgs by navArgs()
        circleCode = args.circleCode
        isCircleHead = args.isCircleHead

        if (currentUser != null) {
            var circleTotal: Float
            var userCount = 0
            var user: User
            var averageExpense: String
            val userList = mutableListOf<User>()
            CoroutineScope(Dispatchers.Main).launch {
                fireStore.collection("users")
                        .whereEqualTo("circleCode", circleCode)
                        .get()
                        .addOnSuccessListener { results ->
                            if (!results.isEmpty) {
                                for (result in results.documents) {
                                    userCount++
                                    user = result.toObject<User>()!!
                                    userList.add(user)
                                }

                                averageExpense = roundIt(userList[0].circleTotal / userCount)
                                tickerViewAverageExpense.text = "$$averageExpense"

                                for (u in userList) {
                                    val fullName = u.firstName + " " + u.lastName
                                    var pays = false
                                    var amt = 0.00F
                                    if (u.totalContribution < averageExpense.toFloat()) {
                                        pays = true
                                        amt = averageExpense.toFloat() - u.totalContribution
                                        amt = roundIt(amt).toFloat()
                                    } else {
                                        pays = false
                                        amt = u.totalContribution - averageExpense.toFloat()
                                        amt = roundIt(amt).toFloat()
                                    }
                                    settleList.add(SettledResultModel(fullName, pays, amt.toString()))
                                }
                                recyclerViewSettledResult.adapter = SettlementDetailAdapter(settleList)
                                recyclerViewSettledResult.layoutManager = LinearLayoutManager(context)
                                recyclerViewSettledResult.setHasFixedSize(true)
                            } else {
                                Log.e(TAG, "onViewCreated: Database Query, Result is Empty")
                            }
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "onViewCreated: ")
                        }
            }
        } else {
            findNavController().navigate(R.id.loginFragment)
        }

        buttonSettleNow.setOnClickListener {
            //prevent double click
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            //Check if the user is CircleHead
            if (isCircleHead) {
                AlertDialog.Builder(requireContext())
                        .setTitle("Are you sure?")
                        .setMessage("Are you sure want to settle? \nThis action cannot be undone. If you do not want to settle now, click 'Cancel' to go back.")
                        .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, p1: Int) {
                                doSettlement()
                            }
                        })
                        .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, p1: Int) {
                                dialog!!.dismiss()
                            }
                        })
                        .show()
            } else {
                AlertDialog.Builder(requireContext())
                        .setTitle("Not Authorized")
                        .setMessage("You are not authorized to make settlement. Please ask your circle head to do settlement.")
                        .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, p1: Int) {
                                dialog!!.dismiss()
                            }
                        })
                        .show()
            }
        }

        buttonDone.setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    private fun doSettlement() {
        val settlement: Settlement = Settlement(circleCode, settleList)
        val settledTimeStamp = System.currentTimeMillis().toString()
        fireStore.collection("settlements").document(settledTimeStamp).set(settlement)
                .addOnSuccessListener {

                    fireStore.collection("users")
                            .whereEqualTo("circleCode", circleCode)
                            .get()
                            .addOnSuccessListener { querySnaps ->
                                if (!querySnaps.isEmpty) {
                                    for (document in querySnaps.documents) {
                                        val docRef = fireStore.collection("users").document(document.id)
                                        fireStore.runBatch { batch ->
                                            batch.update(docRef,"lastSettlementDate",settledTimeStamp)
                                            batch.update(docRef,"circleTotal", 0.0)
                                            batch.update(docRef,"totalContribution", 0.0)
                                        }
                                    }
                                }
                            }


                    Toast.makeText(requireContext(), "Settlement Complete!!!", Toast.LENGTH_SHORT).show();
                    buttonSettleNow.visibility = View.GONE
                    buttonDone.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Log.e(TAG, "doSettlement: Failed to update settlement to the server")
                }
    }


    private fun roundIt(numInFloat: Float): String {
        return "%.2f".format(numInFloat)
    }
}
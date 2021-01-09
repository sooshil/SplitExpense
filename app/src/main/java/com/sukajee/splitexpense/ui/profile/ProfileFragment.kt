package com.sukajee.splitexpense.ui.profile

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sukajee.splitexpense.DrawerLocker
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.UsersContribution
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.DateFormat
import java.util.*


class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRefUser: DatabaseReference
    private lateinit var dbRefCircleCodes: DatabaseReference
    private lateinit var dbRefTransactions: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var textViewGreetUser: TextView
    private lateinit var textViewUsersContribution: TextView
    private var usersContributionList: MutableList<UsersContribution> = ArrayList()
    private lateinit var recyclerOthersContribution: RecyclerView
    private val dateLastSettlement: Long = 1608768000000
    private var contributedUserMap: MutableMap<String, Float> = hashMapOf()
    private lateinit var greetingMessage: String
    private lateinit var userNameReference: DatabaseReference
    private var allUsersTotalContribution: Float = 0.0F
    private lateinit var displayAmount: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerOthersContribution = view.findViewById(R.id.recyclerOthersContribution)

        fun getGreetingMessage(): String {
            val c = Calendar.getInstance()
            val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

            return when (timeOfDay) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..20 -> "Good Evening"
                in 21..23 -> "Good Night"
                else -> "Welcome"
            }
        }
        greetingMessage = getGreetingMessage()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRefUser = database.reference.child("users")
        dbRefCircleCodes = database.reference.child("circle_codes")
        dbRefTransactions = database.reference.child("transactions")

        (activity as DrawerLocker?)!!.unlockDrawer()
        progressBarCircular.isVisible = true
        textViewGreetUser = view.findViewById(R.id.textViewGreetUser)
        textViewUsersContribution = view.findViewById(R.id.textViewYourContributionAmount)


        val currentDate = currentDate()
        textViewYourContribution.text = "Your contribution as of $currentDate"

        //Circle Code
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userReferenceListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val circleCode = snapshot.child("circleCode").value.toString()
                    val firstName = snapshot.child("firstName").value.toString()
                    Log.d("TAG", "The circle code is : $circleCode")
                    Log.d("TAG", "The first name is : $firstName")
                    if (circleCode.isNotEmpty() && circleCode != "") {
                        progressBarCircular.isVisible = false
                        textViewGreetUser.text = "$greetingMessage $firstName"
                    } else {
                        progressBarCircular.isVisible = false
                        val action = ProfileFragmentDirections.actionProfileFragmentToJoinCreateCircleFragment(firstName)
                        findNavController().navigate(action)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
            val userIdReference = dbRefUser.child(user.uid)
            userIdReference.addValueEventListener(userReferenceListener)


            //Contribution Amount Display
            val transactionReferenceListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalAmount = 0.0F
                    for (date in snapshot.children) {
                        if (date.key!!.toLong() > dateLastSettlement) {
                            if (date.child("userId").getValue().toString() == user.uid) {
                                val amount = date.child("amount").getValue().toString()
                                val floatAmount = amount.toFloat()
                                totalAmount += floatAmount

                            }
                        }
                    }
                    displayAmount = roundIt(totalAmount)
                    var percentageFloat = 0.0F
                    if (allUsersTotalContribution != 0.0F) {
                        val percentage = (displayAmount.toFloat() * 100) / allUsersTotalContribution
                        percentageFloat = roundIt(percentage).toFloat()
                        val displayText = "$$displayAmount ($percentageFloat%)"
                        val ss = SpannableString(displayText)
                        ss.setSpan(AbsoluteSizeSpan(20, true), ss.indexOf("(", 0,false), ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        textViewUsersContribution.text = ss
                    } else {
                        textViewUsersContribution.text = "$$displayAmount"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
            dbRefTransactions.addValueEventListener(transactionReferenceListener)


            //All User's Contribution RecyclerView
            dbRefTransactions.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersContributionList.clear()
                    contributedUserMap.clear()
                    var totalAmount = 0.0F
                    allUsersTotalContribution = 0.0F
                    for (date in snapshot.children) {
                        if (date.key!!.toLong() > dateLastSettlement) {
                            val userString = date.child("userId").getValue().toString()
                            if (userString != user.uid) {
                                val amount = date.child("amount").getValue().toString()
                                if (contributedUserMap.containsKey(userString)) {
                                    val oldAmount = contributedUserMap[userString]
                                    totalAmount = oldAmount!!.toFloat() + amount.toFloat()
                                } else {
                                    totalAmount += amount.toFloat()
                                }
                                contributedUserMap.put(userString, totalAmount)
                                totalAmount = 0.0F
                            }
                            val amountX = date.child("amount").getValue().toString()
                            val amountXFloat = amountX.toFloat()
                            allUsersTotalContribution += amountXFloat
                        }
                    }

                    //Display User's first and last name in recuclerview
                    for (userKeys in contributedUserMap) {
                        userNameReference = dbRefUser.child(userKeys.key)
                        var fullName: String
                        userNameReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val amount = userKeys.value
                                val percentageContribution: Float = (amount * 100) / allUsersTotalContribution
                                val toFloat = roundIt(percentageContribution)
                                fullName = "${snapshot.child("firstName").value.toString()} ${snapshot.child("lastName").value.toString()}"
                                usersContributionList.add(UsersContribution(fullName, amount.toString(), toFloat.toFloat()))

                                if (usersContributionList.size == 0) {
                                    textViewNoOtherUsers.visibility = View.VISIBLE
                                }
                                recyclerOthersContribution.adapter = AllUserContributionsAdapter(usersContributionList)
                                recyclerOthersContribution.layoutManager = LinearLayoutManager(requireContext())
                                recyclerOthersContribution.setHasFixedSize(true)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                    }
                    if (allUsersTotalContribution != 0.0F) {
                        val percentage = (displayAmount.toFloat() * 100) / allUsersTotalContribution
                        val percentageFloat = roundIt(percentage).toFloat()
                        val displayText = "$$displayAmount ($percentageFloat%)"
                        val ss = SpannableString(displayText)
                        ss.setSpan(AbsoluteSizeSpan(20, true), ss.indexOf("(", 0,false), ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        textViewUsersContribution.text = ss
                    } else {
                        textViewUsersContribution.text = "$$displayAmount"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        } else {
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        fabAddTransaction.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToAddNewTransactionFragment()
            findNavController().navigate(action)
        }

        textViewSeeAll.setOnClickListener {

        }
    }

    private fun roundIt(numInFloat: Float): String {
        return "%.2f".format(numInFloat)
    }

    fun currentDate(): String {
        val c = Calendar.getInstance()
        return DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
    }

}
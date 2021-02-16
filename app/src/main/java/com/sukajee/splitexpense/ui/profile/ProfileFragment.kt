package com.sukajee.splitexpense.ui.profile

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.sukajee.splitexpense.DrawerLocker
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.User
import com.sukajee.splitexpense.data.UsersContribution
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class ProfileFragment : Fragment(R.layout.fragment_profile), AllUserContributionsAdapter.OnItemCliclListener {

    private var currentUser: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var textViewGreetUser: TextView
    private lateinit var textViewUsersContribution: TickerView
    private var usersContributionList: MutableList<UsersContribution> = ArrayList()
    private lateinit var recyclerOthersContribution: RecyclerView
    private var contributedUserMap: MutableMap<String, Float> = hashMapOf()
    private lateinit var greetingMessage: String
    private var allUsersTotalContribution: Float = 0.0F
    private lateinit var displayAmount: String
    private lateinit var textViewNoOtherUsers: TextView
    private lateinit var progressBarCircular: ProgressBar
    private lateinit var typeface: Typeface
    private lateinit var circleCode: String
    private lateinit var user: User
    private lateinit var allUsers: List<User>
    private lateinit var userId: String
    private var members = mutableListOf<UsersContribution>()
    private lateinit var usersnaps: ListenerRegistration
    private lateinit var userContributionSnaps: ListenerRegistration


    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        textViewNoOtherUsers = view.findViewById(R.id.textViewNoOtherUsers)
        recyclerOthersContribution = view.findViewById(R.id.recyclerOthersContribution)
        progressBarCircular = view.findViewById(R.id.progressBarCircular)

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

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
        } else {
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                    .addOnSuccessListener { documentSnapShot ->
                        val usr = documentSnapShot.toObject<User>()!!
                        if (usr.circleCode == "None") {
                            findNavController().navigate(R.id.joinCreateCircleFragment)
                        }
                    }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        (activity as DrawerLocker?)!!.unlockDrawer()
        progressBarCircular.visibility = View.VISIBLE
        textViewGreetUser = view.findViewById(R.id.textViewGreetUser)
        textViewUsersContribution = view.findViewById(R.id.textViewYourContributionAmount)
        textViewUsersContribution.setCharacterLists(TickerUtils.provideNumberList())
        typeface = ResourcesCompat.getFont(requireContext(), R.font.bai_jamjuree_medium)!!
        textViewUsersContribution.typeface = typeface

        val currentDate = currentDate()
        textViewYourContribution.text = "Your contribution as of $currentDate"

        val args: ProfileFragmentArgs by navArgs()
        val passedUser = args.userObject
        if (passedUser != null) {
            user = passedUser
            circleCode = user.circleCode
            if (circleCode == "None") {
                findNavController().navigate(R.id.joinCreateCircleFragment)
            } else {
                textViewGreetUser.text = "$greetingMessage ${passedUser.firstName}"
            }
        }
        currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            progressBarCircular.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val userRef = fireStore.collection("users").document(currentUser!!.uid)
                usersnaps = userRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val firstName = snapshot.get("firstName").toString()
                        val circleCode = snapshot.get("circleCode").toString()
                        if (circleCode == "None") {
                            val action = ProfileFragmentDirections.actionProfileFragmentToJoinCreateCircleFragment()
                            findNavController().navigate(action)
                        } else {
                            textViewGreetUser.text = "$greetingMessage $firstName"
                            val contributedAmount = snapshot.get("totalContribution").toString().toFloat()
                            val circleTotal = snapshot.get("circleTotal").toString().toFloat()
                            if (circleTotal != 0.0F) {
                                val percentage = (contributedAmount * 100) / circleTotal
                                textViewUsersContribution.text = "$${roundIt(contributedAmount)} (${roundIt(percentage)}%)"
                            } else {
                                val percentage = 0.00F
                                textViewUsersContribution.text = "$${roundIt(contributedAmount)} (${roundIt(percentage)}%)"
                            }


                            userContributionSnaps = fireStore.collection("users")
                                    .whereEqualTo("circleCode", circleCode)
                                    .orderBy("firstName")
                                    .addSnapshotListener { userList, err ->
                                        if (err != null) {
                                            Log.w(TAG, "Listen failed.", err)
                                            return@addSnapshotListener
                                        }
                                        if (userList != null && !userList.isEmpty) {
                                            members.clear()
                                            for(user in userList) {
                                                if(user.id == currentUser!!.uid) {
                                                    continue
                                                } else {
                                                    userId = user.id
                                                    val membersName = "${user.get("firstName")} ${user.get("lastName")}"
                                                    val amount = user.get("totalContribution").toString().toFloat()
                                                    var percentage: Float
                                                    if (amount != 0.0F) {
                                                        percentage = (user.get("totalContribution").toString().toFloat() * 100) / user.get("circleTotal").toString().toFloat()
                                                    } else {
                                                        percentage = 0.00F
                                                    }
                                                    val usersContribution = UsersContribution(userId, membersName, roundIt(amount), roundIt(percentage).toFloat())
                                                    members.add(usersContribution)
                                                }
                                            }
                                            recyclerOthersContribution.adapter = AllUserContributionsAdapter(members, this@ProfileFragment)
                                            recyclerOthersContribution.layoutManager = LinearLayoutManager(context)
                                            recyclerOthersContribution.setHasFixedSize(true)
                                            progressBarCircular.visibility = View.INVISIBLE
                                        }
                                    }
                        }
                    } else {
                        Log.e(TAG, "onViewCreated: snapshot is null")
                    }
                }
            }
        }

        textViewUsersContribution.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToUsersContributionDetailsFragment(currentUser!!.uid)
            findNavController().navigate(action)
        }

        fabAddTransaction.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToAddNewTransactionFragment()
            findNavController().navigate(action)
        }
    }

//    override fun onStop() {
//        super.onStop()
//        if(usersnaps != null) {
//            usersnaps.remove()
//            Log.d(TAG, "onPause: UserSnapsListener removed. No more listening")
//        }
//        if(userContributionSnaps != null) {
//            userContributionSnaps.remove()
//            Log.d(TAG, "onPause: userContributionSnaps listener is removed. No more listening")
//        }
//    }

    private fun roundIt(numInFloat: Float): String {
        return "%.2f".format(numInFloat)
    }

    fun currentDate(): String {
        val c = Calendar.getInstance()
        return DateFormat.getDateInstance(DateFormat.FULL).format(c.time)
    }

    override fun onItemClick(position: Int) {
        val userIdToForward = members[position].userId
        val action = ProfileFragmentDirections.actionProfileFragmentToUsersContributionDetailsFragment(userIdToForward)
        findNavController().navigate(action)
    }
}

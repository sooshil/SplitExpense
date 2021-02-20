package com.sukajee.splitexpense

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sukajee.splitexpense.data.User
import com.sukajee.splitexpense.data.UsersContribution
import com.sukajee.splitexpense.ui.profile.AllUserContributionsAdapter
import com.sukajee.splitexpense.ui.profile.ProfileFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class DoSettlementFragment : Fragment(R.layout.fragment_do_settlement), AllUserContributionsAdapter.OnItemCliclListener {

    private var currentUser: FirebaseUser? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var recyUsersContribution: RecyclerView
    private lateinit var typeface: Typeface
    private lateinit var circleCode: String
    private lateinit var user: User
    private lateinit var userId: String
    private var members = mutableListOf<UsersContribution>()
    private lateinit var usersnaps: ListenerRegistration
    private lateinit var userContributionSnaps: ListenerRegistration
    private lateinit var buttonPreviewSettlement: Button
    private var isCircleHead by Delegates.notNull<Boolean>()


    companion object {
        private const val TAG = "DoSettlementFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_do_settlement, container, false)


        //settle hune bittikai last settle date ahile ko set garne
        //first time user sign up garera create circle garne bittikai tyo date last settle date hunchha
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fireStore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        recyUsersContribution = view.findViewById(R.id.recyUsersContribution)
        buttonPreviewSettlement = view.findViewById(R.id.buttonPreviewSettlement)

        if (currentUser != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val userRef = fireStore.collection("users").document(currentUser!!.uid)
                usersnaps = userRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val firstName = snapshot.get("firstName").toString()
                        isCircleHead = snapshot.get("circleHead") as Boolean
                        circleCode = snapshot.get("circleCode").toString()
                        if (circleCode == "None") {
                            val action = ProfileFragmentDirections.actionProfileFragmentToJoinCreateCircleFragment()
                            findNavController().navigate(action)
                        } else {
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
                                            for (user in userList) {
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
                                            recyUsersContribution.adapter = AllUserContributionsAdapter(members, this@DoSettlementFragment)
                                            recyUsersContribution.layoutManager = LinearLayoutManager(context)
                                            recyUsersContribution.setHasFixedSize(true)
                                        }
                                    }
                        }
                    } else {
                        Log.e(TAG, "onViewCreated: snapshot is null")
                    }
                }
            }
        } else {
            findNavController().navigate(R.id.loginFragment)
        }

        buttonPreviewSettlement.setOnClickListener {
            //go to settlement preview fragment. Here everybody can see preview of current settlement status.
            //But only circleHead can actually make settlement.

            val action = DoSettlementFragmentDirections.actionGlobalSettledResultFragment(circleCode, isCircleHead)
            findNavController().navigate(action)
        }
    }


    override fun onStop() {
        super.onStop()
        if (usersnaps != null) {
            usersnaps.remove()
            Log.d(TAG, "onPause: UserSnapsListener removed. No more listening")
        }
        if (userContributionSnaps != null) {
            userContributionSnaps.remove()
            Log.d(TAG, "onPause: userContributionSnaps listener is removed. No more listening")
        }
    }

    private fun roundIt(numInFloat: Float): String {
        return "%.2f".format(numInFloat)
    }

    override fun onItemClick(position: Int) {
        val userIdToForward = members[position].userId
        val action = DoSettlementFragmentDirections.actionDoSettlementFragmentToUsersContributionDetailsFragment(userIdToForward)
        findNavController().navigate(action)
    }
}
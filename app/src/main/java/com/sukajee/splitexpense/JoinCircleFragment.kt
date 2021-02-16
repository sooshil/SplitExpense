package com.sukajee.splitexpense

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.fragment_join_circle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class JoinCircleFragment : Fragment(R.layout.fragment_join_circle) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mFirstName: String
    private var lastClickTime: Long = 0
    private lateinit var fireStore: FirebaseFirestore
    private var passedUser: User? = null


    companion object {
        private val TAG = "JoinCircleFragment"
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null) {
            val action = JoinCircleFragmentDirections.actionJoinCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        val args: JoinCircleFragmentArgs by navArgs()
        passedUser = args.userObject
        if(passedUser != null) {
            mFirstName = passedUser!!.firstName
            textViewHelloFirstName.text = "Hello $mFirstName"
        } else {
            textViewHelloFirstName.text = "Hello User"
        }

        (activity as DrawerLocker?)!!.lockDrawer()

        if (firebaseAuth.currentUser != null) {

            buttonJoinCircle.setOnClickListener {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = SystemClock.elapsedRealtime()

                val enteredCode = editTextCircleCodeInput.text.toString().trim()
                if (enteredCode.isNotBlank() == true) {
                    CoroutineScope(Dispatchers.Main).launch {
                        addUserToTheCircle(enteredCode)
                    }
                } else {
                    editTextCircleCodeInput.setError("Code can not be blank")
                }
            }

        } else {
            val action = JoinCircleFragmentDirections.actionJoinCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }


    }

    private fun addUserToTheCircle(enteredCode: String) {
        // get circleTotal from the circleHead user
        var circleTotal = 0.0F
        var lastSettlementDate: String = System.currentTimeMillis().toString()
        fireStore.collection("users")
                .whereEqualTo("circleCode", enteredCode)
                .whereEqualTo("circleHead", true)
                .get()
                .addOnSuccessListener { 
                    for(document in it.documents) {
                        circleTotal = document.get("circleTotal").toString().toFloat()
                        lastSettlementDate = document.get("lastSettlementDate").toString()
                    }

                    val userRef = fireStore.collection("users").document(firebaseAuth.currentUser!!.uid)
                    val circleCodeRef = fireStore.collection("circleCodes").document(enteredCode)
                    var user: User?
                    userRef.get()
                            .addOnSuccessListener { documentSnapShot ->
                                if (documentSnapShot != null) {
                                    user = documentSnapShot.toObject<User>()
                                    if(user!!.circleCode == "None") {
                                        userRef.update("circleCode", enteredCode)
                                        userRef.update("circleTotal", circleTotal)
                                        userRef.update("lastSettlementDate", lastSettlementDate)
                                        val circleCodeMap = mapOf(firebaseAuth.currentUser!!.uid to "${user!!.firstName} ${user!!.lastName}")
                                        circleCodeRef.update(circleCodeMap)
                                        Snackbar.make(requireView(),"You have successfully joined to the circle.", Snackbar.LENGTH_SHORT)
                                                .setAction("DISMISS", null)
                                                .show()
                                        val action = JoinCircleFragmentDirections.actionJoinCircleFragmentToProfileFragment()
                                        findNavController().navigate(action)
                                    } else {
                                        Snackbar.make(requireView(),"You are already a part of a circle. Please leave your current circle to join another one.", Snackbar.LENGTH_SHORT)
                                                .setAction("DISMISS", null)
                                                .show()
                                    }
                                }
                            }
                }
                .addOnFailureListener {
                    Log.d(TAG, "addUserToTheCircle: ${it.message}")
                    Toast.makeText(requireContext(), "The code is invalid. Please double check the circle code.", Toast.LENGTH_SHORT).show();
                }
        
    }
}
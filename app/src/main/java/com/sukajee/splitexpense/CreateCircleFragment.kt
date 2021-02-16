package com.sukajee.splitexpense

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_circle.*
import kotlinx.android.synthetic.main.fragment_profile.*


class CreateCircleFragment : Fragment(R.layout.fragment_create_circle) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var mFirstName: String
    private var lastClickTime: Long = 0

    companion object {
        private const val TAG = "CreateCircleFragment"
    }

    override fun onStart() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val action = CreateCircleFragmentDirections.actionCreateCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        super.onStart()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        val args: CreateCircleFragmentArgs by navArgs()
        val passedUser: User? = args.userObject
        mFirstName = passedUser?.firstName ?: "User"
        textViewHelloFirstName.text = "Hello $mFirstName"

        (activity as DrawerLocker?)!!.lockDrawer()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userRef = fireStore.collection("users").document(currentUser.uid)
            var user: User? = passedUser
            var userToPass: User? = null
            buttonGenerateCode.setOnClickListener {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = SystemClock.elapsedRealtime()

                userRef.get()
                        .addOnSuccessListener { docuemntSnapShot ->
                            if (docuemntSnapShot != null) {
                                user = docuemntSnapShot.toObject<User>()!!
                                val circleCode = user!!.circleCode
                                if (circleCode != "None") {
                                    textViewCircleCode.text = circleCode
                                    Snackbar.make(requireView(), "You are already associated with a circle. You can not be associated with two different circle at the same time.", Snackbar.LENGTH_LONG)
                                            .setAction("DISMISS", null)
                                            .show()
                                } else {
                                    val circleCodeRef = fireStore.collection("circleCodes").document()
                                    val circleCodeMap = mapOf(currentUser.uid to "${user!!.firstName} ${user!!.lastName}")
                                    circleCodeRef.set(circleCodeMap)
                                    userRef.update("circleCode", circleCodeRef.id)
                                    userRef.update("circleHead", true)
                                    textViewCircleCode.text = circleCodeRef.id
                                    userToPass = User("", user!!.firstName, user!!.lastName, "", "", circleCode, 0.0F, 0.0F, System.currentTimeMillis().toString(), 0.0F, false)
                                    Snackbar.make(requireView(), "Circle code created successfully.", Snackbar.LENGTH_LONG)
                                            .setAction("DISMISS", null)
                                            .show()
                                    buttonContinue.isEnabled = true
                                }
                            }
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Error fetching circle code ${it.message}")
                        }
            }

            buttonContinue.setOnClickListener {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = SystemClock.elapsedRealtime()
                buttonContinue.isEnabled = false
                val action = CreateCircleFragmentDirections.actionCreateCircleFragmentToProfileFragment(userToPass)
                findNavController().navigate(action)
            }

        } else {
            // if not logged in, go back to login fragment
            val action = CreateCircleFragmentDirections.actionCreateCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }
}




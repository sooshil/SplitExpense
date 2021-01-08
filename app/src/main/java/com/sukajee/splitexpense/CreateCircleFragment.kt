package com.sukajee.splitexpense

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.sukajee.splitexpense.ui.login.LoginFragmentDirections
import com.sukajee.splitexpense.ui.profile.ProfileFragmentDirections
import com.sukajee.splitexpense.util.INVITATION_CODE_LENGTH
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_circle.*
import kotlinx.android.synthetic.main.fragment_profile.*

internal class CreateCircleFragment : Fragment(R.layout.fragment_create_circle) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private var databaseReferenceCircleCode: DatabaseReference? = null
    private var databaseReferenceUser: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private lateinit var mFirstName: String
    private var lastClickTime: Long = 0
    var codeExists: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReferenceCircleCode = database?.reference!!.child("circle_codes")
        databaseReferenceUser = database?.reference!!.child("users")
        val args: CreateCircleFragmentArgs by navArgs()
        mFirstName = args.firstName

        (activity as DrawerLocker?)!!.lockDrawer()

        val user = firebaseAuth.currentUser
        if (user != null) {
            textViewHelloFirstName.text = "Hello $mFirstName"
        }

        buttonCreateCircle.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            if (editTextCircleCodeInput.text?.isNotBlank() == true) {
                val circleCode = editTextCircleCodeInput.text.toString().trim()
                if (circleCode.length != INVITATION_CODE_LENGTH) {
                    editTextCircleCodeInput.setError("Invitation code must be of $INVITATION_CODE_LENGTH characters.")
                    editTextCircleCodeInput.requestFocus()
                } else {
                    //check if the code already exists
                    firebaseUser = firebaseAuth.currentUser!!

                    databaseReferenceCircleCode!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (code in snapshot.children) {
                                if (code.key.toString() == circleCode) {
                                    codeExists = true
                                    Log.d("TAG", "code exists")
                                    Toast.makeText(requireContext(), "That code is not available. Please choose a different one.", Toast.LENGTH_SHORT).show();
                                    break
                                }
                            }
                            if (!codeExists) {
                                val currentUserCircleCodes = databaseReferenceCircleCode?.child(circleCode)
                                val currentUserId = firebaseAuth.currentUser?.uid.toString()
                                currentUserCircleCodes?.apply {
                                    child(currentUserId).setValue(currentUserId)
                                }
                                databaseReferenceUser!!.child(currentUserId).child("circleCode").setValue(circleCode)

                                findNavController().navigate(R.id.profileFragment)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("TAG", error.message)
                        }
                    })

                }
            } else {
                editTextCircleCodeInput.setError("Please enter invitation code.")
                editTextCircleCodeInput.requestFocus()
            }
        }
    }
}
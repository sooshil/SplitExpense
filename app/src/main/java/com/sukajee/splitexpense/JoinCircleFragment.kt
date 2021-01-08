package com.sukajee.splitexpense

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sukajee.splitexpense.util.INVITATION_CODE_LENGTH
import kotlinx.android.synthetic.main.fragment_join_circle.*

internal class JoinCircleFragment : Fragment(R.layout.fragment_join_circle) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mFirstName: String
    private var lastClickTime: Long = 0
    private lateinit var databaseUserReferenceUsers: DatabaseReference
    private lateinit var databaseCircleCodeReference: DatabaseReference
    private lateinit var database: FirebaseDatabase


    companion object {
        private val TAG = "JoinCircleFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseUserReferenceUsers = database.reference.child("users")
        databaseCircleCodeReference = database.reference.child("circle_codes")

        val args: JoinCircleFragmentArgs by navArgs()
        mFirstName = args.firstName

        (activity as DrawerLocker?)!!.lockDrawer()

        if(firebaseAuth.currentUser != null) {
            textViewHelloFirstName.text = "Hello $mFirstName"
        }

        buttonJoinCircle.setOnClickListener {
            if(SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()
            val enteredCode = editTextCircleCodeInput.text.toString().trim()
            if(enteredCode.isNotBlank()  == true) {
                if (enteredCode.length == INVITATION_CODE_LENGTH) {
                    addUserToTheCircle(enteredCode)
                } else {
                    editTextCircleCodeInput.setError("Code must be $INVITATION_CODE_LENGTH characters long.")
                }
            } else {
                editTextCircleCodeInput.setError("Code can not be blank")
            }
        }
    }

    private fun addUserToTheCircle(enteredCode: String) {
        var codeFound : Boolean = false
        databaseCircleCodeReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (code in snapshot.children) {
                    if (code.key.toString() == enteredCode) {
                        codeFound = true
                        Log.d(TAG, "code exists")
                        break
                    }
                }
                if (codeFound) {
                    val currentUserId = firebaseAuth.currentUser!!.uid
                    databaseCircleCodeReference.child(enteredCode).child(currentUserId).setValue(currentUserId)
                    databaseUserReferenceUsers.child(currentUserId).child("circleCode").setValue(enteredCode)
                    findNavController().navigate(R.id.profileFragment)
                }
                if (!codeFound) {
                    Toast.makeText(requireContext(), "Looks like, that code didn't work. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}
package com.sukajee.splitexpense.ui.login

import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.sukajee.splitexpense.DrawerLocker
import com.sukajee.splitexpense.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_profile.*

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var databaseReferenceUser: DatabaseReference? = null
    private var databaseReferenceCircleCodes: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var email: String
    private lateinit var password: String
    private var lastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReferenceUser = database?.reference!!.child("users")
        databaseReferenceCircleCodes = database?.reference!!.child("circle_codes")

        (activity as DrawerLocker?)!!.lockDrawer()

        buttonLogin.setOnClickListener {
            email = editTextEmail.text.toString().trim()
            password = editTextPassword.text.toString().trim()
            login(email, password)
        }

        buttonRegister.setOnClickListener {
            if(SystemClock.elapsedRealtime() - lastClickTime < 5000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()
            findNavController().navigate(action)
        }
    }

    private fun login(emailEntered: String, passwordEntered: String) {
        if (emailEntered.length > 0 && passwordEntered.length > 0) {
            if (isValidEmail(emailEntered)) {
                firebaseAuth?.signInWithEmailAndPassword(emailEntered, passwordEntered)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                firebaseUser = firebaseAuth!!.currentUser!!
                                val userReference = databaseReferenceUser!!.child(firebaseUser.uid)
                                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val firstName = snapshot.child("firstName").value.toString()
                                        if(snapshot.child("circleCode").exists()) {
                                            val circleCode = snapshot.child("circleCode").value.toString()
                                            Log.d("TAG", "The circle code is : $circleCode")
                                            if (circleCode.isNotEmpty() && circleCode != "") {
                                                findNavController().navigate(R.id.profileFragment)
                                            } else {
                                                val action = LoginFragmentDirections.actionLoginFragmentToJoinCreateCircleFragment(firstName)
                                                findNavController().navigate(action)
                                            }
                                        } else {
                                            val action = LoginFragmentDirections.actionLoginFragmentToJoinCreateCircleFragment(firstName)
                                            findNavController().navigate(action)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("TAG", error.message)
                                    }
                                })
                            } else {
                                Toast.makeText(requireContext(), "Login failed, please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(requireContext(), "Login failed. Exception: ${it.message}", Toast.LENGTH_SHORT).show();
                        }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please enter username and password to login.", Toast.LENGTH_SHORT).show();
        }
    }

    fun isValidEmail(emailToValidate: CharSequence): Boolean {
        return !TextUtils.isEmpty(emailToValidate) && Patterns.EMAIL_ADDRESS.matcher(emailToValidate).matches()
    }
}
package com.sukajee.splitexpense.ui.login

import android.app.ProgressDialog
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
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.sukajee.splitexpense.DrawerLocker
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var fireStore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var email: String
    private lateinit var password: String
    private var lastClickTime: Long = 0
    private lateinit var progressBar: ProgressBar


    companion object {
        private const val TAG = "LoginFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        progressBar = view.findViewById(R.id.progressBar)

        (activity as DrawerLocker?)!!.lockDrawer()

        buttonLogin.setOnClickListener {
            email = editTextEmail.text.toString().trim()
            password = editTextPassword.text.toString().trim()
            progressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                login(email, password)
            }
        }

        buttonRegister.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
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
                firebaseAuth.signInWithEmailAndPassword(emailEntered, passwordEntered)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                firebaseUser = firebaseAuth.currentUser!!
                                var circleCode = ""
                                var firstName = ""
                                val userRef = fireStore.collection("users").document(firebaseUser.uid)
                                userRef.get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot != null) {
                                                val user = documentSnapshot.toObject<User>()
                                                circleCode = user!!.circleCode
                                                firstName = user.firstName
                                                if (circleCode != "None") {
                                                    progressBar.visibility = View.INVISIBLE
                                                    val action = LoginFragmentDirections.actionLoginFragmentToProfileFragment(user)
                                                    findNavController().navigate(action)
                                                } else {
                                                    progressBar.visibility = View.INVISIBLE
                                                    val action = LoginFragmentDirections.actionLoginFragmentToJoinCreateCircleFragment(user)
                                                    findNavController().navigate(action)
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Log.e(TAG, it.message.toString())
                                        }
                            } else {
                                Toast.makeText(requireContext(), "Login failed, please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Login failed. Exception: ${it.message}", Toast.LENGTH_SHORT).show();
                        }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please enter username and password to login.", Toast.LENGTH_SHORT).show();
        }
        progressBar.visibility = View.INVISIBLE
    }

    fun isValidEmail(emailToValidate: CharSequence): Boolean {
        return !TextUtils.isEmpty(emailToValidate) && Patterns.EMAIL_ADDRESS.matcher(emailToValidate).matches()
    }

}
package com.sukajee.splitexpense.ui.registration

import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import android.widget.Toast.makeText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sukajee.splitexpense.DrawerLocker
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    val registrationViewModel: RegistrationViewModel by viewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firstNameValue: String
    private lateinit var lastNameValue: String
    private lateinit var emailValue: String
    private lateinit var phoneValue: String
    private lateinit var passwordValue: String
    private lateinit var confirmPasswordValue: String
    private var circleCode: String? = null
    private var lastClickTime: Long = 0

    companion object {
        private const val TAG = "RegistrationFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        (activity as DrawerLocker?)!!.lockDrawer()

        buttonRegister.setOnClickListener {
            progressBarCircular.visibility = View.VISIBLE
            if(SystemClock.elapsedRealtime() - lastClickTime < 5000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            CoroutineScope(Dispatchers.Main).launch {
                registerUser()
            }
        }

        buttonLogin.setOnClickListener {
            if(SystemClock.elapsedRealtime() - lastClickTime < 5000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            val action = RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        setHasOptionsMenu(true)
    }

    private fun registerUser() {
        firstNameValue = editTextFirstName.text.toString().trim()
        lastNameValue = editTextLastName.text.toString().trim()
        emailValue = editTextEmail.text.toString().trim()
        phoneValue = editTextPhone.text.toString().trim()
        passwordValue = editTextPassword.text.toString().trim()
        confirmPasswordValue = editTextConfirmPassword.text.toString().trim()
        circleCode = "None"

        if (!anyEmptyFields()) {
            if (isValidEmail(emailValue)) {
                if (isGoodPhoneLength()) {
                    if (isPassAndConfirmPassMatching()) {
                        if (isValidPassword(passwordValue)) {
                            firebaseAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.e(TAG, "Registration Successful")
                                            val user = User(firebaseAuth.currentUser!!.uid, firstNameValue, lastNameValue, emailValue, phoneValue, circleCode!!, 0.0F, 0.0F,"", "", 0.0F, false)
                                            firestore.collection("users").document(firebaseAuth.currentUser!!.uid).set(user)
                                            Snackbar.make(requireView(),"Registration Successful. Please Login.",Snackbar.LENGTH_LONG)
                                                    .setAction("DISMISS", null)
                                                    .show()

                                            progressBarCircular.visibility = View.INVISIBLE
                                            val action = RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
                                            findNavController().navigate(action)
                                        } else {
                                            Log.e(TAG, "Registration Unsuccessful")
                                            progressBarCircular.visibility = View.INVISIBLE
                                            Snackbar.make(requireView(),"Registration Failed, Please try again", Snackbar.LENGTH_SHORT)
                                                    .setAction("DISMISS", null)
                                                    .show()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, exception.toString())
                                        progressBarCircular.visibility = View.GONE
                                    }

                        } else {
                            linearLayoutPasswordRequirements.visibility = View.VISIBLE
                            clearPasswordFields()
                            makeText(requireContext(), R.string.password_is_not_valid, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        clearPasswordFields()
                        makeText(requireContext(), R.string.password_doesnt_match, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    makeText(requireContext(), R.string.phone_requirement_invalid, Toast.LENGTH_SHORT).show()
                }
            } else {
                makeText(requireContext(), R.string.enter_valid_email, Toast.LENGTH_SHORT).show()
            }
        } else {
            makeText(requireContext(), R.string.enter_value_in_all_fields, Toast.LENGTH_SHORT).show()
        }
    }

    fun isValidEmail(emailToValidate: CharSequence): Boolean {
        return !TextUtils.isEmpty(emailToValidate) && Patterns.EMAIL_ADDRESS.matcher(emailToValidate).matches()
    }

    fun isValidPassword(password: CharSequence): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun clearPasswordFields() {
        editTextPassword.setText("")
        editTextConfirmPassword.setText("")
    }

    fun anyEmptyFields(): Boolean {
        return !(firstNameValue.length > 0 && lastNameValue.length > 0 && emailValue.length > 0 && phoneValue.length > 0 && passwordValue.length > 0)
    }

    fun isGoodPhoneLength(): Boolean {
        return phoneValue.length == 10
    }

    fun isPassAndConfirmPassMatching(): Boolean {
        return passwordValue == confirmPasswordValue
    }

}

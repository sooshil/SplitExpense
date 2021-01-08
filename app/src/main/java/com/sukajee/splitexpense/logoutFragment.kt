package com.sukajee.splitexpense

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.sukajee.splitexpense.ui.profile.ProfileFragmentDirections

internal class logoutFragment : Fragment(R.layout.fragment_logout) {
    
    private var firebaseAuth: FirebaseAuth? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth!!.currentUser

        if (user != null) {
            firebaseAuth!!.signOut()
            findNavController().navigate(R.id.loginFragment)
        } else {
            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
            findNavController().navigate(R.id.loginFragment)
        }
    }

}
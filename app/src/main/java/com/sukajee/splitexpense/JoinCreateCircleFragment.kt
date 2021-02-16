package com.sukajee.splitexpense

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sukajee.splitexpense.data.User
import kotlinx.android.synthetic.main.fragment_create_circle.*
import kotlinx.android.synthetic.main.fragment_join_create_circle.*
import kotlinx.android.synthetic.main.fragment_join_create_circle.buttonCreateCircle
import kotlinx.android.synthetic.main.fragment_join_create_circle.textViewHelloFirstName
import kotlinx.android.synthetic.main.fragment_registration.*

class JoinCreateCircleFragment : Fragment(R.layout.fragment_join_create_circle) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mFirstName: String
    private var lastClickTime: Long = 0


    override fun onStart() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val action = JoinCreateCircleFragmentDirections.actionJoinCreateCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        super.onStart()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val args: JoinCreateCircleFragmentArgs by navArgs()
        val passedUser: User? = args.userObject
        mFirstName = passedUser?.firstName ?: "User"

        (activity as DrawerLocker?)!!.lockDrawer()

        val user = firebaseAuth.currentUser
        if (user != null) {
            textViewHelloFirstName.text = "Hello $mFirstName"
        } else {
            val action = JoinCreateCircleFragmentDirections.actionJoinCreateCircleFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        buttonJoinCircle.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()

            val action = JoinCreateCircleFragmentDirections.actionJoinCreateCircleFragmentToJoinCircleFragment(passedUser)
            findNavController().navigate(action)
        }

        buttonCreateCircle.setOnClickListener {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return@setOnClickListener
            }
            lastClickTime = SystemClock.elapsedRealtime()
            val action = JoinCreateCircleFragmentDirections.actionJoinCreateCircleFragmentToCreateCircleFragment(passedUser)
            findNavController().navigate(action)
        }
    }
}
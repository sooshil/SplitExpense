package com.sukajee.splitexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class DoSettlementFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_do_settlement, container, false)


        //settle hune bittikai last settle date ahile ko set garne
        //first time user sign up garera create circle garne bittikai tyo date last settle date hunchha
    }
}
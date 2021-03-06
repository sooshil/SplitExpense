package com.sukajee.splitexpense.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.UsersContribution
import kotlinx.android.synthetic.main.others_contribution_list_item.view.*

class AllUserContributionsAdapter(
        private val contributionList: List<UsersContribution>,
        private val listener: OnItemCliclListener
) : RecyclerView.Adapter<AllUserContributionsAdapter.ViewHolder>() {

    private lateinit var textViewContributionAmount: TickerView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.others_contribution_list_item, parent, false)

        textViewContributionAmount = itemView.findViewById(R.id.textViewContributionAmount)

        val typeface = ResourcesCompat.getFont(itemView.context, R.font.bai_jamjuree_medium)!!
        textViewContributionAmount.typeface = typeface

        textViewContributionAmount.setCharacterLists(TickerUtils.provideNumberList())
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = contributionList[position]

        holder.userName.text = currentItem.usersName


        holder.contributionAmount.text = "$${currentItem.amount} (${currentItem.percentage}%)"
    }

    override fun getItemCount() = contributionList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val userName : TextView = itemView.textViewUserName
        val contributionAmount : TickerView = itemView.textViewContributionAmount

        init {
            itemView.setOnClickListener (this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemCliclListener {
        fun onItemClick(position: Int)
    }
}

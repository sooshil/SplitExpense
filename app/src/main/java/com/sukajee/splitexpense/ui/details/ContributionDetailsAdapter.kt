package com.sukajee.splitexpense.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.sukajee.splitexpense.R
import com.sukajee.splitexpense.data.Transaction
import kotlinx.android.synthetic.main.others_contribution_list_item.view.*
import kotlinx.android.synthetic.main.users_contribution_detail_list_item.view.*
import java.lang.String.format
import java.text.DateFormat
import java.text.DateFormat.MEDIUM
import java.text.SimpleDateFormat
import java.util.*

class ContributionDetailsAdapter(private val transactionList: List<Transaction>) : RecyclerView.Adapter<ContributionDetailsAdapter.ViewHolder>() {

    private lateinit var textViewAmount: TickerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.users_contribution_detail_list_item, parent, false)

        textViewAmount = itemView.findViewById(R.id.textViewAmount)

        val typeface = ResourcesCompat.getFont(itemView.context, R.font.bai_jamjuree_medium)!!
        textViewAmount.typeface = typeface

        textViewAmount.setCharacterLists(TickerUtils.provideNumberList())
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = transactionList[position]
        var previousItem = transactionList[position]

        if(position > 0) {
            previousItem = transactionList[position - 1]
        }

        if(getDate(currentItem.dateOfTransaction.toString().toLong()) == getDate(previousItem.dateOfTransaction.toString().toLong())) {
            holder.transactionDate.visibility = View.GONE
        } else {
            holder.transactionDate.text = getDate(currentItem.dateOfTransaction.toString().toLong())
        }

        if (position == 0) {
            holder.transactionDate.visibility = View.VISIBLE
            holder.transactionDate.text = getDate(currentItem.dateOfTransaction.toString().toLong())
        }

        holder.storeName.text = currentItem.storeName
        holder.amount.text = "$${currentItem.amount}"
        holder.description.text = currentItem.description
    }

    override fun getItemCount() = transactionList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val storeName: TextView = itemView.textViewStore
        val transactionDate: TextView = itemView.textViewTransactionDate
        val description: TextView = itemView.textViewDescription
        val amount: TickerView = itemView.textViewAmount


    }

    fun getDate(milliSeconds: Long): String? {
        val calendar: Calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
    }
}
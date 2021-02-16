package com.sukajee.splitexpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.sukajee.splitexpense.data.SettledResultModel
import kotlinx.android.synthetic.main.settle_detail_list_item.view.*
import kotlinx.android.synthetic.main.users_contribution_detail_list_item.view.*
import java.text.DateFormat
import java.util.*

class SettlementDetailAdapter (private val settlementResultList: List<SettledResultModel>) : RecyclerView.Adapter<SettlementDetailAdapter.ViewHolder> () {

    private lateinit var textViewPaysAmount: TickerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settle_detail_list_item, parent, false)

        textViewPaysAmount = itemView.findViewById(R.id.textViewPaysAmount)

        val typeface = ResourcesCompat.getFont(itemView.context, R.font.bai_jamjuree_medium)!!
        textViewPaysAmount.typeface = typeface
        textViewPaysAmount.setCharacterLists(TickerUtils.provideNumberList())
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = settlementResultList[position]

        holder.userName.text = currentItem.userName
        if(currentItem.isPays) {
            holder.textViewPays.text = " Pays"
        } else {
            holder.textViewPays.text = " Receives"
        }
        holder.textViewPaysAmount.text = currentItem.payOrReceiveAmount
    }

    override fun getItemCount() = settlementResultList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName : TextView = itemView.textViewUserName
        val textViewPays: TextView = itemView.textViewPays
        val textViewPaysAmount: TickerView = itemView.textViewPaysAmount

    }
}
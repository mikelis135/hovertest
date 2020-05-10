package com.usehover.hovertest.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.usehover.hovertest.R
import com.usehover.hovertest.event.OnTransactionSelectedListener
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import kotlinx.android.synthetic.main.transaction_item.view.*

class HomeAdapter(private var transactionList: ArrayList<Transaction>, val listener: OnTransactionSelectedListener?) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transaction_item, parent, false))
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    fun updateTransaction(transactionList: ArrayList<Transaction>) {
        this.transactionList = transactionList
        notifyDataSetChanged()
    }

    fun removeTransaction(position: Int): ArrayList<Transaction> {
        transactionList.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
        return transactionList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactionList[position], position)
    }

    inner class ViewHolder(private val containerView: View) : RecyclerView.ViewHolder(containerView),
            View.OnClickListener {

        override fun onClick(v: View?) {
            listener?.onTransactionSelected(transaction)
        }

        private lateinit var transaction: Transaction

        fun bind(transaction: Transaction, position: Int) {

            val details = transaction.phone + transaction.accountNumberValue
            containerView.details.text = details
            containerView.amount.text = transaction.transactionType.name + " - " + transaction.amount

            when (transaction.transactionType) {
                TransactionTypes.Airtime -> containerView.transactionIcon.setImageResource(R.drawable.ic_call_black_24dp)
                TransactionTypes.Data -> containerView.transactionIcon.setImageResource(R.drawable.ic_internet)
                TransactionTypes.Transfer -> containerView.transactionIcon.setImageResource(R.drawable.ic_send)
            }
            containerView.deleteBtn.setOnClickListener {
                listener?.onTransactionDelete(position)
            }

            containerView.rootTransaction.setOnClickListener {
                listener?.onTransactionSelected(transaction)
            }
        }

    }
}
package com.usehover.hovertest.event

import com.usehover.hovertest.model.Transaction

interface OnTransactionSelectedListener {
    fun onTransactionSelected(transaction: Transaction)
    fun onTransactionDelete(position: Int)
}
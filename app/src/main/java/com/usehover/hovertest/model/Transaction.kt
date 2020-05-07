package com.usehover.hovertest.model

data class Transaction(
        val transactionType: TransactionTypes,
        val amount: String,
        val phone: String,
        val message: String,
        val isOthers: Boolean,
        val simOSReportedHni : String,
        val details: String = ""

)
package com.usehover.hovertest.model

data class Transaction(
        val transactionType: TransactionTypes,
        val amount: String = "",
        val phone: String = "",
        var message: String = "",
        val isOthers: Boolean = false,
        val dataOptionValue: String = "",
        val accountOptionValue: String = "",
        val accountNumberValue: String = "",
        var simOSReportedHni: String = "",
        val details: String = ""

)
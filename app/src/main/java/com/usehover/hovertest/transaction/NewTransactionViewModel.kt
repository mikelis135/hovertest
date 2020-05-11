package com.usehover.hovertest.transaction

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.usehover.hovertest.event.SingleEvent
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import com.usehover.hovertest.store.PrefManager
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import javax.inject.Inject

class NewTransactionViewModel @Inject constructor(private val prefManager: PrefManager) : ViewModel() {

    var advertMessageLd: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    var transactionLd: MutableLiveData<String> = MutableLiveData()
    var buttonMessageLd: MutableLiveData<String> = MutableLiveData()
    var animatePhoneLd: MutableLiveData<Boolean> = MutableLiveData()
    var showBankLayoutLd: MutableLiveData<Boolean> = MutableLiveData()
    var transactionTypeLd: MutableLiveData<TransactionTypes?> = MutableLiveData()
    var othersSwitchLd: MutableLiveData<String> = MutableLiveData()
    var setUpAirtimeLd: MutableLiveData<Boolean> = MutableLiveData()
    var setUpDataLd: MutableLiveData<Boolean> = MutableLiveData()
    var setUpTransferLd: MutableLiveData<Boolean> = MutableLiveData()
    var processAirtimeforSelfLd: MutableLiveData<Boolean> = MutableLiveData()
    var processDataforSelfLd: MutableLiveData<Boolean> = MutableLiveData()
    var processDataBundleRequestForSelfLd: MutableLiveData<Boolean> = MutableLiveData()
    var processTransferforSameBankLd: MutableLiveData<Boolean> = MutableLiveData()
    var processAccountResolveRequestLd: MutableLiveData<Boolean> = MutableLiveData()
    var messageLd: MutableLiveData<String> = MutableLiveData()
    var amountValueLd: MutableLiveData<String> = MutableLiveData()
    var accountNumberValueLd: MutableLiveData<String> = MutableLiveData()
    var phoneValueLd: MutableLiveData<String> = MutableLiveData()
    var saveTransactionLd: MutableLiveData<Boolean> = MutableLiveData()
    var simOSReportedHniValueLd: MutableLiveData<String> = MutableLiveData()
    var bundleValueLd: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var accountValueLd: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var toastLd: MutableLiveData<String> = MutableLiveData()

    private var bundleOption = arrayListOf<String>()
    private var bundleValue = arrayListOf<String>()

    private var accountOption = arrayListOf<String>()
    private var accountValue = arrayListOf<String>()

    init {
        setUpActions()
    }

    fun setUpActions() {

        prefManager.fetchActions()?.forEach {

            if (it.name.contains("advert", true)) {
                val random = 1 + (Math.random() * it.name.split("\\n").size - 2).toInt()
                prefManager.advert = it.name.split("\\n")[random]
                advertMessageLd.value = SingleEvent(it.name.split("\\n")[random])
            }

            if (it.name.contains(prefManager.bankName.toString(), true)) {

                when {
                    it.name.contains("airtime self", true) -> prefManager.airtimeSelfAction = it.id
                    it.name.contains("account balance", true) -> prefManager.accountBalanceAction = it.id
                    it.name.contains("airtime others", true) -> prefManager.airtimeOthersAction = it.id
                    it.name.contains("data bundle", true) -> prefManager.dataBundleAction = it.id
                    it.name.contains("account resolve", true) -> prefManager.accountResolveAction = it.id
                    it.name.contains("data self", true) -> prefManager.dataSelfAction = it.id
                    it.name.contains("data others", true) -> prefManager.dataOthersAction = it.id
                    it.name.contains("transfer self", true) -> prefManager.transferSelfAction = it.id
                    it.name.contains("transfer others", true) -> prefManager.transferOthersAction = it.id
                }

            }
        }

    }

    fun getTransferSelfAction(): String? {
        return prefManager.transferSelfAction
    }

    fun getAirtimeOthersAction(): String? {
        return prefManager.airtimeOthersAction
    }

    fun getAirtimeSelfAction(): String? {
        return prefManager.airtimeSelfAction
    }

    fun getTransferOthersAction(): String? {
        return prefManager.transferOthersAction
    }

    fun getDataOthersAction(): String? {
        return prefManager.dataOthersAction
    }

    fun getDataSelfAction(): String? {
        return prefManager.dataSelfAction
    }

    fun getAccountBalanceAction(): String? {
        return prefManager.accountBalanceAction
    }

    fun getAccountResolveAction(): String? {
        return prefManager.accountResolveAction
    }

    fun getDataBundleAction(): String? {
        return prefManager.dataBundleAction
    }

    fun getSimOSReportedHniValue(): String? {
        return prefManager.simOSReportedHni
    }

    fun validateTransaction(transactionType: TransactionTypes?, amount: String, phoneNumber: String, message: String, accountNumber: String, accountOption: String, transactionSelected: String, bundleValue: String, isOthersChecked: Boolean, isSaveTransactionChecked: Boolean) {
        when (transactionType) {
            TransactionTypes.Airtime -> validateAirtime(isOthersChecked, phoneNumber, amount, message, transactionSelected, isSaveTransactionChecked, transactionType)
            TransactionTypes.Data -> validateData(isOthersChecked, phoneNumber, amount, bundleValue, message, transactionSelected, isSaveTransactionChecked, transactionType)
            TransactionTypes.Transfer -> validateTransfer(isOthersChecked, accountNumber, amount, accountOption, message, transactionSelected, isSaveTransactionChecked, transactionType)
            TransactionTypes.Balance -> {
            }
        }
    }

    fun setUpTransaction(transactionType: TransactionTypes?) {
        when (transactionType) {
            TransactionTypes.Airtime -> setUpAirtimeLd.value = true
            TransactionTypes.Data -> setUpDataLd.value = true
            TransactionTypes.Transfer -> setUpTransferLd.value = true
            TransactionTypes.Balance -> {
            }
        }
    }


    private fun processTransaction(transactionItemSelected: String, isSaveTransactionChecked: Boolean, phoneNumber: String, accountNumber: String, transactionType: TransactionTypes, isOthersChecked: Boolean) {

        transactionLd.value = when {
            transactionItemSelected.contains(TransactionTypes.Airtime.name, true) -> prefManager.buyingAirtime
            transactionItemSelected.contains(TransactionTypes.Data.name, true) -> prefManager.buyingData
            transactionItemSelected.contains(TransactionTypes.Transfer.name, true) -> prefManager.sendingMoney
            transactionItemSelected.contains(TransactionTypes.Balance.name, true) -> prefManager.accountBalance
            else -> ""
        }

        checkActionRequest(isSaveTransactionChecked, phoneNumber, accountNumber, transactionType, isOthersChecked)

    }

    fun checkTransactionType(transactionSelected: String, isOthersChecked: Boolean) {

        when {
            transactionSelected.contains(TransactionTypes.Airtime.name, true) -> {
                setUpAirtimeLd.value = true
                buttonMessageLd.value = prefManager.buAirtime
                setUpText(prefManager.self, prefManager.others, isOthersChecked)
                transactionTypeLd.value = TransactionTypes.Airtime
            }
            transactionSelected.contains(TransactionTypes.Data.name, true) -> {
                setUpDataLd.value = true
                setUpText(prefManager.self, prefManager.others, isOthersChecked)
                buttonMessageLd.value = prefManager.getDataBundles
                setUpText(prefManager.self, prefManager.others, isOthersChecked)
                transactionTypeLd.value = TransactionTypes.Data
            }
            else -> {
                setUpTransferLd.value = true
                buttonMessageLd.value = prefManager.sendMoney
                setUpText("to \t" + prefManager.bankName, prefManager.otherBanks, isOthersChecked)
                transactionTypeLd.value = TransactionTypes.Transfer
            }
        }
        return
    }

    private fun setUpText(offText: String, onText: String, isOthersChecked: Boolean) {
        if (isOthersChecked) {
            othersSwitchLd.value = onText
        } else {
            othersSwitchLd.value = offText
        }
    }

    fun checkDataBundleRequest(phoneNumber: String, isdataForOthers: Boolean, isSaveTransactionChecked: Boolean) {

        messageLd.value = "\nProcessing...\n\n"
        transactionLd.value = "\nGetting Data Bundle"
        saveTransactionLd.value = isSaveTransactionChecked
        phoneValueLd.value = phoneNumber
        prefManager.simOSReportedHni?.let {
            simOSReportedHniValueLd.value = it
        }

        when (isdataForOthers) {

            TRUE -> {
                processDataBundleRequestForSelfLd.value = false

            }
            FALSE -> {
                processDataBundleRequestForSelfLd.value = true
            }
        }

    }

    fun checkAccountResolveRequest(accountNumber: String) {

        prefManager.simOSReportedHni?.let {
            simOSReportedHniValueLd.value = it
        }

        accountNumberValueLd.value = accountNumber

        processAccountResolveRequestLd.value = true

    }

    private fun checkActionRequest(isSaveTransactionChecked: Boolean, phoneNumber: String, accountNumber: String, transactionType: TransactionTypes?, isOthersChecked: Boolean) {

        saveTransactionLd.value = isSaveTransactionChecked
        phoneValueLd.value = phoneNumber
        accountNumberValueLd.value = accountNumber

        prefManager.simOSReportedHni?.let {
            simOSReportedHniValueLd.value = it
        }

        when (transactionType) {

            TransactionTypes.Airtime -> when (isOthersChecked) {

                TRUE -> {
                    processAirtimeforSelfLd.value = false
                }
                FALSE -> {
                    processAirtimeforSelfLd.value = true
                }
            }

            TransactionTypes.Data -> when (isOthersChecked) {

                TRUE -> {
                    processDataforSelfLd.value = false
                }
                FALSE -> {
                    processDataforSelfLd.value = true
                }
            }
            TransactionTypes.Transfer -> when (isOthersChecked) {

                TRUE -> {
                    processTransferforSameBankLd.value = false
                }
                FALSE -> {
                    processTransferforSameBankLd.value = true
                }
            }
            TransactionTypes.Balance -> {
            }
        }

    }

    private fun validateAirtime(isOthersChecked: Boolean, phoneNumber: String, amount: String, message: String, transactionSelected: String, isSaveTransactionChecked: Boolean, transactionType: TransactionTypes?) {

        saveTransactionLd.value = isSaveTransactionChecked

        when (isOthersChecked) {

            TRUE -> {
                if (phoneNumber.isNotEmpty() && amount.isNotEmpty()) {
                    messageLd.value = "\nPhone : $phoneNumber\nTransaction : $transactionSelected\nAmount :  ₦$amount\n\n"
                    amountValueLd.value = amount
                    transactionType?.let {
                        processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, "", it, isOthersChecked)
                    }
                } else {
                    toastLd.value = prefManager.enterValidPhoneNumber
                }
            }
            FALSE -> {
                if (amount.isNotEmpty()) {
                    messageLd.value = "\nTransaction : $transactionSelected\nAmount :  ₦$amount\n\n"
                    amountValueLd.value = amount
                    transactionType?.let {
                        processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, "", it, isOthersChecked)
                    }
                } else {
                    toastLd.value = prefManager.enterValidAmount
                }
            }

        }
    }

    private fun validateData(isOthersChecked: Boolean, phoneNumber: String, amount: String, bundleValue: String, message: String, transactionSelected: String, isSaveTransactionChecked: Boolean, transactionType: TransactionTypes?) {

        saveTransactionLd.value = isSaveTransactionChecked
        phoneValueLd.value = phoneNumber

        when (isOthersChecked) {

            TRUE -> {
                if (bundleValue.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    messageLd.value = "\nPhone : $phoneNumber\nTransaction : $transactionSelected\n\n"
                    amountValueLd.value = amount
                    transactionType?.let {
                        processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, "", it, isOthersChecked)
                    }
                } else if (bundleValue.isEmpty() && phoneNumber.isNotEmpty()) {
                    messageLd.value = "\nProcessing...\n\n"
                    transactionLd.value = "\nGetting Data Bundle"
                    processDataBundleRequestForSelfLd.value = true
                    checkDataBundleRequest(phoneNumber, isOthersChecked, isSaveTransactionChecked)
                } else if (phoneNumber.isEmpty()) {
                    toastLd.value = prefManager.enterValidPhoneNumber
                }
            }

            FALSE -> {

                messageLd.value = "\nTransaction : $transactionSelected\n\n"
                if (bundleValue.isEmpty()) {
                    messageLd.value = "\nProcessing...\n\n"
                    transactionLd.value = "\nGetting Data Bundle"
                    processDataBundleRequestForSelfLd.value = false
                } else {
                    messageLd.value = "\nTransaction : $transactionSelected\nData plan :  $bundleValue\n\n"
                    amountValueLd.value = bundleValue
                    transactionType?.let {
                        processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, "", it, isOthersChecked)
                    }
                }
            }

        }
    }

    private fun validateTransfer(isOthersChecked: Boolean, accountNumber: String, amount: String, accountOption: String, phoneNumber: String, transactionSelected: String, isSaveTransactionChecked: Boolean, transactionType: TransactionTypes?) {

        if (accountNumber.isNotEmpty() && amount.isNotEmpty()) {

            amountValueLd.value = amount
            saveTransactionLd.value = isSaveTransactionChecked

            if (isOthersChecked && accountOption.isEmpty()) {
                processAccountResolveRequestLd.value = true
            } else if (isOthersChecked && accountOption.isNotEmpty()) {
                transactionType?.let {
                    processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, accountNumber, it, isOthersChecked)
                }
            } else if (isOthersChecked) {
                transactionType?.let {
                    processTransaction(transactionSelected, isSaveTransactionChecked, phoneNumber, accountNumber, it, isOthersChecked)
                }
            }
        } else {
            toastLd.value = prefManager.enterValidAccountNumberAndNumber
        }
    }

    fun checkSwitch(transactionSelected: String, isOthersChecked: Boolean, accountOption: String, transactionType: TransactionTypes?) {

        when (isOthersChecked) {

            TRUE -> {
                othersSwitchLd.value = prefManager.others

                when {

                    transactionSelected.contains(TransactionTypes.Airtime.name, true) -> {
                        buttonMessageLd.value = prefManager.buAirtime
                        setUpText(prefManager.self, prefManager.others, isOthersChecked)
                        animatePhoneLd.value = true
                    }

                    transactionSelected.contains(TransactionTypes.Data.name, true) -> {
                        animatePhoneLd.value = true
                    }

                    transactionSelected.contains(TransactionTypes.Transfer.name, true) -> {
                        when (accountOption.isNotEmpty()) {
                            TRUE -> showBankLayoutLd.value = true
                            FALSE -> showBankLayoutLd.value = false
                        }
                        othersSwitchLd.value = prefManager.otherBanks
                        buttonMessageLd.value = prefManager.verifyAccount
                    }
                }
            }

            FALSE -> {

                othersSwitchLd.value = prefManager.self

                when {

                    transactionSelected.contains(TransactionTypes.Airtime.name, true) -> {
                        transactionTypeLd.value = TransactionTypes.Airtime
                        animatePhoneLd.value = false
                    }

                    transactionSelected.contains(TransactionTypes.Data.name, true) -> {
                        animatePhoneLd.value = false
                    }

                    transactionSelected.contains(TransactionTypes.Transfer.name, true) -> {
                        showBankLayoutLd.value = false
                        othersSwitchLd.value = "to\t" + prefManager.bankName
                        buttonMessageLd.value = prefManager.sendMoney
                    }
                }
            }
        }
    }

    fun processResult(transaction: String, sessionResponse: Array<String>) {

        if (transaction.contains("data bundle", true)) {

            populateDataBundle(sessionResponse)

        } else if (transaction.contains("resolving account", true)) {
            resolveAccount(sessionResponse)
        }
    }

    private fun populateDataBundle(sessionResponse: Array<String>) {
        buttonMessageLd.value = prefManager.buyData

        sessionResponse.let {

            try {
                val bundle = it.last().substringAfter("\n").substringBeforeLast("\n")
                if (bundle.isNotEmpty()) {
                    val bundleArray = bundle.split("\n")

                    bundleArray.forEach {
                        val bundleDetails = it.split("-")

                        if (!bundleDetails.isNullOrEmpty()) {
                            bundleOption.add(bundleDetails[0])
                            bundleValue.add(bundleDetails[1])
                        } else {
                            bundleValue.add(it)
                        }

                    }

                    bundleValueLd.value = bundleValue
                }
            } catch (ignore: Exception) {

            }

        }
    }

    private fun resolveAccount(sessionResponse: Array<String>) {

        buttonMessageLd.value = prefManager.sendMoney
        sessionResponse.let {

            try {

                val bundle = it.last().substringAfter("\n\n").substringBeforeLast("\n")
                if (bundle.isNotEmpty()) {
                    val bundleArray = bundle.split("\n")
                    Log.d(TAG, bundleArray.toString())
                    bundleArray.forEach {
                        val accountDetails = it.split(".")

                        if (!accountDetails.isNullOrEmpty()) {
                            accountOption.add(accountDetails[0])
                            accountValue.add(accountDetails[1])
                        } else {
                            accountValue.add(it)
                        }

                    }

                    accountValueLd.value = accountValue

                }
            } catch (ignore: Exception) {

            }
        }

    }

    fun saveTransaction(transactionType: TransactionTypes?, amountValue: String, phoneValue: String, message: String, isOthersChecked: Boolean, dataOptionValue: String, accountOptionValue: String, accountNumberValue: String, simOSReportedHniValue: String) {
        transactionType?.let {
            prefManager.saveTransaction(Transaction(it, "₦$amountValue", phoneValue, message, isOthersChecked, dataOptionValue, accountOptionValue, accountNumberValue, simOSReportedHniValue))
        }
    }

}
package com.usehover.hovertest.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.usehover.hovertest.event.SingleEvent
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import com.usehover.hovertest.store.PrefManager
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import javax.inject.Inject

class HomeViewModel @Inject constructor(
        private val prefManager: PrefManager
) : ViewModel() {

    val simLd: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()
    val transactionLd: MutableLiveData<String> = MutableLiveData()
    val amountLd: MutableLiveData<String> = MutableLiveData()
    val showWelcomeLd: MutableLiveData<Boolean> = MutableLiveData()
    val welcomeCheckLd: MutableLiveData<Boolean> = MutableLiveData()
    val actionsLd: MutableLiveData<Boolean> = MutableLiveData()
    val setUpLd: MutableLiveData<Boolean> = MutableLiveData()
    val setPermissionLd: MutableLiveData<Boolean> = MutableLiveData()
    val getBalanceLd: MutableLiveData<String> = MutableLiveData()
    val voiceLd: MutableLiveData<String> = MutableLiveData()
    val advertMessageLd: MutableLiveData<String> = MutableLiveData()
    val transactionListLd: MutableLiveData<ArrayList<Transaction>> = MutableLiveData()
    var processAirtimeForSelfLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
    var processDataForSelfLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
    var processTransferForSameBankLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
    var processBalanceLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()

//    var transferSelfActionLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
//    var processBalanceLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
//    var processBalanceLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
//    var processBalanceLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()
//    var processBalanceLd: MutableLiveData<HashMap<Boolean, Transaction>> = MutableLiveData()

    private val airtimeHashMap = hashMapOf<Boolean, Transaction>()
    private val dataHashMap = hashMapOf<Boolean, Transaction>()
    private val transferHashMap = hashMapOf<Boolean, Transaction>()
    private val balanceHashMap = hashMapOf<Boolean, Transaction>()

    private var transferSelfAction = ""
    private var airtimeOthersAction = ""
    private var airtimeSelfAction = ""
    private var dataOthersAction = ""
    private var dataSelfAction = ""
    private var accountBalanceAction = ""

    init {
        setUpAction()
    }

    fun fetchSim(permission: Boolean) {
        simLd.value = SingleEvent(prefManager.fetchSim().isNullOrEmpty())
        if (permission) {
            setPermissionLd.value = prefManager.fetchSim().isNullOrEmpty()
        }
    }

    fun saveTransaction(newTransactionList: ArrayList<Transaction>) {
        prefManager.saveTransactions(newTransactionList)
    }

    fun setTransaction(transactionValue: String) {

        when {
            transactionValue.contains(TransactionTypes.Airtime.name, true) -> transactionLd.value = prefManager.buyingAirtime
            transactionValue.contains(TransactionTypes.Data.name, true) -> transactionLd.value = prefManager.buyingData
            transactionValue.contains(TransactionTypes.Transfer.name, true) -> transactionLd.value = prefManager.sendingMoney
            else -> {
            }
        }
    }

    fun checkAction(transaction: Transaction) {

        amountLd.value = transaction.amount.replace("₦", "")

        when (transaction.transactionType) {

            TransactionTypes.Airtime -> when (transaction.isOthers) {

                TRUE -> {
                    airtimeHashMap[false] = transaction
                    processAirtimeForSelfLd.value = airtimeHashMap
                }
                FALSE -> {
                    airtimeHashMap[true] = transaction
                    processAirtimeForSelfLd.value = airtimeHashMap
                }
            }

            TransactionTypes.Data -> when (transaction.isOthers) {

                TRUE -> {
                    dataHashMap[false] = transaction
                    processDataForSelfLd.value = dataHashMap
                }
                FALSE -> {
                    airtimeHashMap[true] = transaction
                    processDataForSelfLd.value = dataHashMap
                }
            }
            TransactionTypes.Transfer -> when (transaction.isOthers) {

                TRUE -> {
                    transferHashMap[false] = transaction
                    processTransferForSameBankLd.value = transferHashMap
                }
                FALSE -> {
                    transferHashMap[true] = transaction
                    processTransferForSameBankLd.value = transferHashMap
                }
            }
            TransactionTypes.Balance -> {
                balanceHashMap[true] = transaction
                processBalanceLd.value = balanceHashMap
            }

        }

    }

    fun setUpAction() {

        prefManager.fetchActions()?.forEach {

            if (it.name.contains("advert", true)) {
                val random = 1 + (Math.random() * it.name.split("\\n").size - 2).toInt()
                prefManager.advert = it.name.split("\\n")[random]
                advertMessageLd.value = it.name.split("\\n")[random]
            }

            if (it.name.contains(prefManager.bankName.toString(), true)) {

                when {
                    it.name.contains("account balance", true) -> prefManager.accountBalanceAction = it.id
                    it.name.contains("airtime self", true) -> prefManager.airtimeSelfAction = it.id
                    it.name.contains("airtime others", true) -> prefManager.airtimeOthersAction = it.id
                    it.name.contains("data self", true) -> prefManager.dataSelfAction = it.id
                    it.name.contains("data others", true) -> prefManager.dataOthersAction = it.id
                    it.name.contains("transfer gtb", true) -> prefManager.transferSelfAction = it.id
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

    fun fetchTransactions() {
        if (!prefManager.fetchTransactions().isNullOrEmpty()) {
            showWelcomeLd.value = false

            transactionListLd.value = prefManager.fetchTransactions()


        } else {
            showWelcomeLd.value = true
        }
    }

    fun checkVoice(whatToSay: String) {
        when (prefManager.voiceEnable) {
            TRUE -> voiceLd.value = whatToSay
        }
    }

    fun fetchActions(hasError: Boolean, getBalance: Boolean = false, welcomeCheck: Boolean = false) {

        if (prefManager.fetchActions().isNullOrEmpty()) {
            when (hasError) {
                TRUE -> actionsLd.value = false
                FALSE -> actionsLd.value = true
            }

        }

        if (getBalance) {
            getBalanceLd.value = prefManager.simOSReportedHni
        }

        if (welcomeCheck) {
            welcomeCheckLd.value = prefManager.fetchActions().isNullOrEmpty()

        } else {

        }
    }

    fun saveActions(actions: ArrayList<HoverAction>) {
        prefManager.saveActions(actions)
    }

    fun saveSim(simList: MutableList<SimInfo>) {
        prefManager.saveSim(simList)
        if (prefManager.simOSReportedHni.isNullOrEmpty()) {
            prefManager.simOSReportedHni = simList[0].osReportedHni
        }
    }

    fun saveBankName(bankName: String?) {
        bankName?.let {
            prefManager.bankName = it
        }
    }

    fun setUp() {
        setUpLd.value = prefManager.fetchActions().isNullOrEmpty()
    }
}
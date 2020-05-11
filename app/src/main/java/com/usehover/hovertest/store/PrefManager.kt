package com.usehover.hovertest.store

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.usehover.hovertest.model.Transaction
import java.util.*
import javax.inject.Inject

class PrefManager @Inject constructor(private val prefModel: PrefModel, val gson: Gson) {

    private var sharedPrefs: SharedPreferences = prefModel.sharedPrefs
    private var editor: SharedPreferences.Editor = prefModel.editor

    var isFirstTimeLaunch: Boolean
        get() = sharedPrefs.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    var voiceEnable: Boolean
        get() = sharedPrefs.getBoolean(VOICEENABLE, false)
        set(voiceEnable) {
            editor.putBoolean(VOICEENABLE, voiceEnable)
            editor.commit()
        }

    var bankName: String?
        get() = sharedPrefs.getString(BANKNAME, "")
        set(bankName) {
            editor.putString(BANKNAME, bankName)
            editor.commit()
        }

    var bankPosition: Int
        get() = sharedPrefs.getInt(BANKPOSITION, 0)
        set(bankPosition) {
            editor.putInt(BANKPOSITION, bankPosition)
            editor.commit()
        }

    var simOSReportedHni: String?
        get() = sharedPrefs.getString(SIMREPORTEDHNI, "")
        set(simOSReportedHni) {
            editor.putString(SIMREPORTEDHNI, simOSReportedHni)
            editor.commit()
        }

    var simPosition: Int
        get() = sharedPrefs.getInt(SIMPOSITION, 0)
        set(simPosition) {
            editor.putInt(SIMPOSITION, simPosition)
            editor.commit()
        }


    var accountBalanceAction: String?
        get() = sharedPrefs.getString(ACCOUNTBALANCEACTIONID, "")
        set(accountBalanceAction) {
            editor.putString(ACCOUNTBALANCEACTIONID, accountBalanceAction)
            editor.commit()
        }

    var airtimeSelfAction: String?
        get() = sharedPrefs.getString(AIRTIMESELFACTIONID, "")
        set(airtimeSelfAction) {
            editor.putString(AIRTIMESELFACTIONID, airtimeSelfAction)
            editor.commit()
        }

    var airtimeOthersAction: String?
        get() = sharedPrefs.getString(AIRTIMEOTHERSACTIONID, "")
        set(airtimeOthersAction) {
            editor.putString(AIRTIMEOTHERSACTIONID, airtimeOthersAction)
            editor.commit()
        }


    var dataBundleAction: String?
        get() = sharedPrefs.getString(DATABUNDLEACTIONID, "")
        set(dataBundleAction) {
            editor.putString(DATABUNDLEACTIONID, dataBundleAction)
            editor.commit()
        }

    var accountResolveAction: String?
        get() = sharedPrefs.getString(ACCOUNTRESOLVEACTIONID, "")
        set(accountResolveAction) {
            editor.putString(ACCOUNTRESOLVEACTIONID, accountResolveAction)
            editor.commit()
        }

    var dataSelfAction: String?
        get() = sharedPrefs.getString(DATASELFACTIONID, "")
        set(dataSelfAction) {
            editor.putString(DATASELFACTIONID, dataSelfAction)
            editor.commit()
        }

    var dataOthersAction: String?
        get() = sharedPrefs.getString(DATAOTHERSACTIONID, "")
        set(dataOthersAction) {
            editor.putString(DATAOTHERSACTIONID, dataOthersAction)
            editor.commit()
        }

    var transferSelfAction: String?
        get() = sharedPrefs.getString(TRANSFERSELFACTIONID, "")
        set(transferSelfAction) {
            editor.putString(TRANSFERSELFACTIONID, transferSelfAction)
            editor.commit()
        }

    var transferOthersAction: String?
        get() = sharedPrefs.getString(TRANSFEROTHERSACTIONID, "")
        set(transferOthersAction) {
            editor.putString(TRANSFEROTHERSACTIONID, transferOthersAction)
            editor.commit()
        }

    var advert: String?
        get() = sharedPrefs.getString(ADVERT, "")
        set(advert) {
            editor.putString(ADVERT, advert)
            editor.commit()
        }

    private inline fun <reified T> genericFetch(key: String): T? {
        val json = sharedPrefs.getString(key, "")
        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> genericSave(t: T, key: String) {
        val type = object : TypeToken<T>() {}.type
        val json = gson.toJson(t, type)
        editor.putString(key, json).apply()
    }

    fun saveSim(actions: MutableList<SimInfo>) {
        genericSave(actions, SIMNAME)
    }

    fun fetchSim(): MutableList<SimInfo>? {
        return genericFetch<MutableList<SimInfo>>(SIMNAME)
    }

    fun saveActions(actions: ArrayList<HoverAction>) {
        genericSave(actions, ACTIONS)
    }

    fun fetchActions(): ArrayList<HoverAction>? {
        return genericFetch<ArrayList<HoverAction>>(ACTIONS)
    }


    fun fetchTransactions(): ArrayList<Transaction>? {
        return genericFetch<ArrayList<Transaction>>(TRANSACTION)
    }

    fun saveTransactions(transactionList: ArrayList<Transaction>) {
        genericSave(transactionList, TRANSACTION)
    }

    fun saveTransaction(transaction: Transaction): ArrayList<Transaction> {
        var transactionList = fetchTransactions()

        if (transactionList.isNullOrEmpty()) {
            transactionList = ArrayList()
            transactionList.add(transaction)
            genericSave(transactionList, TRANSACTION)
        } else {
            transactionList.add(0, transaction)
            genericSave(transactionList, TRANSACTION)
        }
        return transactionList
    }

    init {
        editor.apply()

    }

    companion object {

        // Shared preferences file name
        private val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private val BANKNAME = "bankname"
        private val VOICEENABLE = "voiceenable"
        private val SIMREPORTEDHNI = "simreportedhni"
        private val SIMNAME = "simname"
        private val BANKPOSITION = "bankposition"
        private val SIMPOSITION = "simposition"
        private val AIRTIMESELFACTIONID = "airtimeself"
        private val AIRTIMEOTHERSACTIONID = "airtimeothers"
        private val DATASELFACTIONID = "dataself"
        private val DATABUNDLEACTIONID = "dataselfbundle"
        private val DATAOTHERSACTIONID = "dataothers"
        private val TRANSFERSELFACTIONID = "transferself"
        private val TRANSFEROTHERSACTIONID = "transferothers"
        private val ACCOUNTRESOLVEACTIONID = "accountresolve"
        private val ACCOUNTBALANCEACTIONID = "accountbalance"
        private val ACTIONS = "actions"
        private val TRANSACTION = "transaction"
        private val ADVERT = "advert"

    }

    val self = "Self\t"
    val others = "Others\t"
    val otherBanks = "Other banks\t"
    val save = "Save\t"
    val dontSave = "Don\'t save\t"
    val buAirtime = "Buy Airtime"
    val buyData = "Buy Data"
    val sendMoney = "Send Money"
    val buyingAirtime = "Buying Airtime"
    val buyingData = "Buying Data"
    val getDataBundles = "Get data bundles"
    val sendingMoney = "Sending Money"
    val accountBalance = "Account Balance"
    val verifyAccount = "Verify Account"
    val enterValidPhoneNumber = "Please enter a valid phone number and amount"
    val enterValidAccountNumberAndNumber = "Please enter a valid account number and amount"
    val enterValidAmount = "Please enter an amount"
}

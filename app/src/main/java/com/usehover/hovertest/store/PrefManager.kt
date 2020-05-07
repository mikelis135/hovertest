package com.usehover.hovertest.store

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hover.sdk.actions.HoverAction
import com.usehover.hovertest.model.Transaction
import java.util.ArrayList

class PrefManager(private val _context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor
    // shared pref mode
    private val PRIVATE_MODE = 0

    private val gson: Gson by lazy {
        Gson()
    }

    var bankName: String?
        get() = pref.getString(BANKNAME, "")
        set(bankName) {
            editor.putString(BANKNAME, bankName)
            editor.commit()
        }

    var bankPosition: Int
        get() = pref.getInt(BANKPOSITION, 0)
        set(bankPosition) {
            editor.putInt(BANKPOSITION, bankPosition)
            editor.commit()
        }

    var simOSReportedHni: String?
        get() = pref.getString(SIMREPORTEDHNI, "")
        set(simOSReportedHni) {
            editor.putString(SIMREPORTEDHNI, simOSReportedHni)
            editor.commit()
        }

    var simPosition: Int
        get() = pref.getInt(SIMPOSITION, 0)
        set(simPosition) {
            editor.putInt(SIMPOSITION, simPosition)
            editor.commit()
        }


    var airtimeSelfAction: String?
        get() = pref.getString(AIRTIMESELFACTIONID, "")
        set(airtimeSelfAction) {
            editor.putString(AIRTIMESELFACTIONID, airtimeSelfAction)
            editor.commit()
        }

    var airtimeOthersAction: String?
        get() = pref.getString(AIRTIMEOTHERSACTIONID, "")
        set(airtimeOthersAction) {
            editor.putString(AIRTIMEOTHERSACTIONID, airtimeOthersAction)
            editor.commit()
        }

    var dataSelfAction: String?
        get() = pref.getString(DATASELFACTIONID, "")
        set(dataSelfAction) {
            editor.putString(DATASELFACTIONID, dataSelfAction)
            editor.commit()
        }

    var dataOthersAction: String?
        get() = pref.getString(DATAOTHERSACTIONID, "")
        set(dataOthersAction) {
            editor.putString(DATAOTHERSACTIONID, dataOthersAction)
            editor.commit()
        }

    var transferSelfAction: String?
        get() = pref.getString(TRANSFERSELFACTIONID, "")
        set(transferSelfAction) {
            editor.putString(TRANSFERSELFACTIONID, transferSelfAction)
            editor.commit()
        }

    var transferOthersAction: String?
        get() = pref.getString(TRANSFEROTHERSACTIONID, "")
        set(transferOthersAction) {
            editor.putString(TRANSFEROTHERSACTIONID, transferOthersAction)
            editor.commit()
        }

    var advert: String?
        get() = pref.getString(ADVERT, "")
        set(advert) {
            editor.putString(ADVERT, advert)
            editor.commit()
        }

    private inline fun <reified T> genericFetch(key: String): T? {
        val json = pref.getString(key, "")
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
        val json = Gson().toJson(t, type)
        editor.putString(key, json).apply()
    }

    fun saveSim(actions: ArrayList<String>) {
        genericSave(actions, SIMNAME)
    }

    fun fetchSim(): ArrayList<String>? {
        return genericFetch<ArrayList<String>>(SIMNAME)
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
            transactionList.add(transaction)
            genericSave(transactionList, TRANSACTION)
        }
        return transactionList
    }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
        editor.apply()
    }

    companion object {

        // Shared preferences file name
        private val PREF_NAME = "hover"
        private val BANKNAME = "bankname"
        private val SIMREPORTEDHNI = "simreportedhni"
        private val SIMNAME = "simname"
        private val BANKPOSITION = "bankposition"
        private val SIMPOSITION = "simposition"
        private val AIRTIMESELFACTIONID = "airtimeself"
        private val AIRTIMEOTHERSACTIONID = "airtimeothers"
        private val DATASELFACTIONID = "dataself"
        private val DATAOTHERSACTIONID = "dataothers"
        private val TRANSFERSELFACTIONID = "transferself"
        private val TRANSFEROTHERSACTIONID = "transferothers"
        private val ACTIONS = "actions"
        private val TRANSACTION = "transaction"
        private val ADVERT = "advert"
    }


}

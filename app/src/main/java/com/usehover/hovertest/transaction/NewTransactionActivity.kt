package com.usehover.hovertest.transaction

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.hover.sdk.api.HoverParameters
import com.usehover.hovertest.R
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import com.usehover.hovertest.model.TransactionTypes.*
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.store.PrefManager
import kotlinx.android.synthetic.main.activity_new_transaction.*
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import androidx.transition.Fade
import androidx.transition.TransitionManager


class NewTransactionActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    private val TAG = "NewTransactionActivity"
    private var message = ""
    private var buttonMessage = ""
    private var advertMessage = ""
    private var transaction = ""
    private var simOSReportedHniValue = ""
    private var details = ""
    private var amountValue = "0"
    private var accountNumberValue = "0"
    private var dataOptionValue = "0"
    private var accountOptionValue = "0"
    private var phoneValue = "0"
    private var saveTransaction = false

    var bundleOption = arrayListOf<String>()
    var bundleValue = arrayListOf<String>()

    var accountOption = arrayListOf<String>()
    var accountValue = arrayListOf<String>()


    private lateinit var prefManager: PrefManager
    private var transactionType: TransactionTypes? = null
    private lateinit var hoverParameters: HoverParameters.Builder
    private var lastClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_transaction)

        setSupportActionBar(toolbar)

        prefManager = PrefManager(this)

        checkTransactionType()

        checkSwitch()

        amount.setOnEditorActionListener(this)

        payOfflineBtn.setOnClickListener {
            pay()
        }

    }

    override fun onEditorAction(textview: TextView?, actionId: Int, event: KeyEvent?): Boolean {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            textview?.let {
                val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
                pay()
                return true
            }

        }

        return false
    }


    private fun pay() {
        singleClick()
        message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
        validateTransaction()
    }

    private fun getDataBundles() {
        singleClick()
        message = "\nProcessing...\n\n"
        transaction = "\nGetting Data Bundle"

        try {
            val hoverRequest = checkDataBundleRequest()
            val intent = hoverRequest.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun getAccountResolve() {
        singleClick()
        message = "\nProcessing...\n\n"
        transaction = "\nResolving Account Number"

        try {
            val hoverRequest = checkAccountResolveRequest()
            val intent = hoverRequest.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun singleClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        setUpActions()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

                bundleOption.clear()
                bundleValue.clear()

                accountOption.clear()
                accountValue.clear()


                if (transaction.contains("data bundle", true)) {

                    payOfflineBtn.text = getString(R.string.buy_data)
                    val sessionTextArr = it.getStringArrayExtra("session_messages")
                    sessionTextArr?.let {

                        try {
                            val bundle = it.last().substringAfter("\n").substringBeforeLast("\n")
                            if (bundle.isNotEmpty()) {
                                val bundleArray = bundle.split("\n")
                                Log.d(TAG, bundleArray.toString())
                                bundleArray.forEach {
                                    val bundleDetails = it.split("-")

                                    if (!bundleDetails.isNullOrEmpty()) {
                                        bundleOption.add(bundleDetails[0])
                                        bundleValue.add(bundleDetails[1])
                                    } else {
                                        bundleValue.add(it)
                                    }

                                }

                                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, bundleValue)
                                dataBundleSP.adapter = arrayAdapter
                            }
                        } catch (ignore: Exception) {

                        }

                    }

                }

                if (transaction.contains("resolving account", true)) {

                    payOfflineBtn.text = getString(R.string.send_money)
                    val sessionTextArr = it.getStringArrayExtra("session_messages")
                    sessionTextArr?.let {

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

                                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, accountValue)
                                bankSP.adapter = arrayAdapter
                            }
                        } catch (ignore: Exception) {

                        }
                    }

                }

                if (saveTransaction && !transaction.contains("data bundle", true) && !transaction.contains("account resolve", true)) {

                    transactionType?.let {
                        prefManager.saveTransaction(Transaction(it, "₦$amountValue", phoneValue, message, othersSwt.isChecked, dataOptionValue, simOSReportedHniValue))

                    }

                } else {

                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_ussd, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.ussd -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpActions() {

        prefManager.fetchActions()?.forEach {

            if (it.name.contains("advert", true)) {
                val random = 1 + (Math.random() * it.name.split("\\n").size - 2).toInt()
                prefManager.advert = it.name.split("\\n")[random]
                advertMessage = it.name.split("\\n")[random]
            }

            if (it.name.contains(prefManager.bankName.toString(), true)) {

                when {
                    it.name.contains("airtime self", true) -> prefManager.airtimeSelfAction = it.id
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

    private fun validateTransaction() {
        when (transactionType) {
            AIRTIME -> validateAirtime()
            DATA -> validateData()
            TRANSFER -> validateTransfer()
        }
    }

    private fun setUpTransaction() {
        when (transactionType) {
            AIRTIME -> setUpAirtime()
            DATA -> setUpData()
            TRANSFER -> setUpTransfer()
        }
    }

    private fun processTransaction() {

        transaction = when {
            transactionTypeSP.selectedItem.toString().contains(AIRTIME.name, true) -> getString(R.string.buying_airtime)
            transactionTypeSP.selectedItem.toString().contains(DATA.name, true) -> getString(R.string.buying_data)
            else -> getString(R.string.sending_money)
        }

        try {
            val hoverRequest = checkActionRequest()
            val intent = hoverRequest.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }

    }

    private fun animate(show: Boolean, layout: ConstraintLayout) {

        val transition = Fade()
        transition.duration = 400L
        transition.addTarget(layout)
        transition.addTarget(saveTransactionSwt)

        TransitionManager.beginDelayedTransition(viewGroup, transition)

        if (show) {
            layout.visibility = View.VISIBLE
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun phoneVisible(isVisible: Boolean) {
        if (isVisible) {
            animate(true, phoneNumberLayout)
        } else {
            animate(false, phoneNumberLayout)
        }
    }

    private fun setUpAirtime() {
        dataBundleLayout.visibility = View.GONE
        accountNumberLayout.visibility = View.GONE
        phoneVisible(othersSwt.isChecked)
        animate(true, amountLayout)
        animate(false, bankLayout)
        buttonMessage = getString(R.string.buy_airtime)
    }

    private fun setUpData() {
        accountNumberLayout.visibility = View.GONE
        amountLayout.visibility = View.GONE
        phoneVisible(othersSwt.isChecked)
        animate(true, dataBundleLayout)
        animate(false, bankLayout)
        buttonMessage = getString(R.string.get_data_bundle)
    }

    private fun setUpTransfer() {
        dataBundleLayout.visibility = View.GONE
        phoneNumberLayout.visibility = View.GONE
        animate(true, amountLayout)
        animate(true, accountNumberLayout)
        animate(true, bankLayout)
        buttonMessage = getString(R.string.send_money)
    }

    private fun validateAirtime() {

        when (othersSwt.isChecked) {

            TRUE -> {
                if (phoneNumber.text.toString().isNotEmpty() && amount.text.toString().isNotEmpty()) {
                    message = "\nPhone : " + phoneNumber.text.toString() + "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                    amountValue = amount.text.toString()
                    processTransaction()
                } else {
                    Toast.makeText(this, getString(R.string.enter_valid_phone_amount), Toast.LENGTH_LONG).show()
                }
            }
            FALSE -> {
                if (amount.text.toString().isNotEmpty()) {
                    message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                    amountValue = amount.text.toString()
                    processTransaction()
                } else {
                    Toast.makeText(this, getString(R.string.enter_valid_amount), Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun validateData() {

        when (othersSwt.isChecked) {

            TRUE -> {
                if (phoneNumber.text.toString().isNotEmpty()) {
                    message = "\nPhone : " + phoneNumber.text.toString() + "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\n\n"
                    amountValue = amount.text.toString()
                    processTransaction()
                } else {
                    message = "\nPhone : " + phoneNumber.text.toString() + "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nData plan :  " + bundleValue[dataBundleSP.selectedItemPosition] + "\n\n"
                    Toast.makeText(this, getString(R.string.enter_valid_phone), Toast.LENGTH_LONG).show()
                }
            }
            FALSE -> {
                message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\n\n"

                if (bundleValue.isEmpty()) {
                    getDataBundles()
                } else {
                    message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nData plan :  " + bundleValue[dataBundleSP.selectedItemPosition] + "\n\n"
                    amountValue = bundleValue[dataBundleSP.selectedItemPosition]
                    processTransaction()
                }

            }

        }
    }

    private fun validateTransfer() {
        if (accountNumber.text.toString().isNotEmpty() && amount.text.toString().isNotEmpty()) {
            amountValue = amount.text.toString()
            if (othersSwt.isChecked && accountOption.isEmpty()) {
                getAccountResolve()
            } else {
                processTransaction()
            }
        } else {
            Toast.makeText(this, getString(R.string.enter_valid_account_amount), Toast.LENGTH_LONG).show()
        }
    }

    private fun checkTransactionType() {

        transactionTypeSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                transactionType = when {
                    transactionTypeSP.selectedItem.toString().contains(AIRTIME.name, true) -> {
                        setUpAirtime()
                        AIRTIME
                    }
                    transactionTypeSP.selectedItem.toString().contains(DATA.name, true) -> {
                        setUpData()
                        DATA
                    }
                    else -> {
                        setUpTransfer()
                        TRANSFER
                    }
                }

                payOfflineBtn.text = buttonMessage
            }

        }

    }

    private fun checkDataBundleRequest(): HoverParameters.Builder {

        saveTransaction = saveTransactionSwt.isChecked
        phoneValue = phoneNumber.text.toString()
        prefManager.simOSReportedHni?.let {
            simOSReportedHniValue = it
        }

        when (othersSwt.isChecked) {

            TRUE -> {
                hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                        .setSim(simOSReportedHniValue)
                        .initialProcessingMessage(message + advertMessage)
                        .setHeader(transaction).request(prefManager.dataOthersAction)
                        .extra("phone", phoneValue)

            }
            FALSE -> {
                hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                        .setSim(simOSReportedHniValue)
                        .initialProcessingMessage(message + advertMessage)
                        .setHeader(transaction).request(prefManager.dataBundleAction)
            }
        }

        return hoverParameters
    }

    private fun checkAccountResolveRequest(): HoverParameters.Builder {

        prefManager.simOSReportedHni?.let {
            simOSReportedHniValue = it
        }
        accountNumberValue = accountNumber.text.toString()

        hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                .setSim(simOSReportedHniValue)
                .initialProcessingMessage(message + advertMessage)
                .setHeader(transaction).request(prefManager.accountResolveAction)
                .extra("amount", amountValue)
                .extra("account", accountNumberValue)


        return hoverParameters
    }


    private fun checkActionRequest(): HoverParameters.Builder {

        saveTransaction = saveTransactionSwt.isChecked
        phoneValue = phoneNumber.text.toString()
        accountNumberValue = accountNumber.text.toString()

        prefManager.simOSReportedHni?.let {
            simOSReportedHniValue = it
        }

        when (transactionType) {

            AIRTIME -> when (othersSwt.isChecked) {

                TRUE -> {
                    hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                            .setSim(simOSReportedHniValue)
                            .initialProcessingMessage(message + advertMessage)
                            .setHeader(transaction).request(prefManager.airtimeOthersAction)
                            .extra("phone", phoneValue)
                            .extra("amount", amountValue)
                }
                FALSE -> {
                    hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                            .setSim(simOSReportedHniValue)
                            .initialProcessingMessage(message + advertMessage)
                            .setHeader(transaction).request(prefManager.airtimeSelfAction)
                            .extra("amount", amountValue)
                }
            }

            DATA -> when (othersSwt.isChecked) {

                TRUE -> {
                    dataOptionValue = bundleOption[dataBundleSP.selectedItemPosition]
                    hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                            .setSim(simOSReportedHniValue)
                            .initialProcessingMessage(message + advertMessage)
                            .setHeader(transaction).request(prefManager.dataOthersAction)
                            .extra("phone", phoneValue)
                            .extra("option", dataOptionValue)

                }
                FALSE -> {
                    dataOptionValue = bundleOption[dataBundleSP.selectedItemPosition]
                    hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                            .setSim(simOSReportedHniValue)
                            .initialProcessingMessage(message + advertMessage)
                            .setHeader(transaction).request(prefManager.dataSelfAction)
                            .extra("option", dataOptionValue)
                }
            }
            TRANSFER -> {
                accountOptionValue = accountOption[bankSP.selectedItemPosition]
                hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                        .setSim(simOSReportedHniValue)
                        .initialProcessingMessage(message + advertMessage)
                        .setHeader(transaction).request(prefManager.transferOthersAction)
                        .extra("amount", amountValue)
                        .extra("account", accountNumberValue)
                        .extra("option", accountOptionValue)
            }
        }

        return hoverParameters
    }

    private fun checkSwitch() {

        othersSwt.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                othersSwt.text = getString(R.string.others)
                if (transactionTypeSP.selectedItem.toString().contains(TRANSFER.name, true)) {
                    payOfflineBtn.text = getString(R.string.verify_account)
                }
            } else {
                othersSwt.text = getString(R.string.self)
                if (transactionTypeSP.selectedItem.toString().contains(TRANSFER.name, true)) {
                    payOfflineBtn.text = getString(R.string.send_money)
                }
            }
            setUpTransaction()
        }

        saveTransactionSwt.setOnCheckedChangeListener { _, isChecked ->
            saveTransaction = isChecked
            if (isChecked) {
                saveTransactionSwt.text = getString(R.string.save)

            } else {
                saveTransactionSwt.text = getString(R.string.dont_save)
            }
        }

    }

}

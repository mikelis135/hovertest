package com.usehover.hovertest.transaction

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.R
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import com.usehover.hovertest.model.TransactionTypes.*
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.store.PrefManager
import kotlinx.android.synthetic.main.activity_new_transaction.*
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.*


class NewTransactionActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    private val TAG = "NewTransactionActivity"
    private var message = ""
    private var buttonMessage = ""
    private var advertMessage = ""
    private var transaction = ""
    private var simOSReportedHniValue = ""
    private var details = ""
    private var amountValue = "0"
    private var phoneValue = "0"
    private var saveTransaction = false

    private lateinit var prefManager: PrefManager
    private lateinit var transactionType: TransactionTypes
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
                message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                processTransaction()
                return true
            }

        }

        return false
    }


    private fun pay() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
        validateTransaction()
    }

    override fun onResume() {
        setUpActions()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
                if (saveTransaction) {
                    prefManager.saveTransaction(Transaction(transactionType, "₦$amountValue", phoneValue, message, othersSwt.isChecked, simOSReportedHniValue))
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

        amountValue = amount.text.toString()
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

    private fun isAmountVisible(isVisible: Boolean) {

        if (isVisible) {
            amount.visibility = View.VISIBLE
            amountTitle.visibility = View.VISIBLE
        } else {
            amount.visibility = View.GONE
            amountTitle.visibility = View.GONE
        }
    }

    private fun accountNumberVisible(isVisible: Boolean) {

        if (isVisible) {
            accountNumber.visibility = View.VISIBLE
            accountNumberTitle.visibility = View.VISIBLE
        } else {
            accountNumber.visibility = View.GONE
            accountNumberTitle.visibility = View.GONE
        }
    }

    private fun phoneVisible(isVisible: Boolean) {

        if (isVisible) {
            phoneNumber.visibility = View.VISIBLE
            phoneNumberTitle.visibility = View.VISIBLE
        } else {
            phoneNumber.visibility = View.GONE
            phoneNumberTitle.visibility = View.GONE
        }
    }

    private fun setUpAirtime() {
        isAmountVisible(true)
        accountNumberVisible(false)
        phoneVisible(othersSwt.isChecked)
        buttonMessage = getString(R.string.buy_airtime)
    }

    private fun setUpData() {
        isAmountVisible(false)
        accountNumberVisible(false)
        phoneVisible(othersSwt.isChecked)
        buttonMessage = getString(R.string.buy_data)

    }

    private fun setUpTransfer() {
        isAmountVisible(true)
        accountNumberVisible(true)
        phoneVisible(false)
        buttonMessage = getString(R.string.send_money)

    }

    private fun validateAirtime() {

        when (othersSwt.isChecked) {

            TRUE -> {
                if (phoneNumber.text.toString().isNotEmpty() && amount.text.toString().isNotEmpty()) {
                    message = "\nPhone : " + phoneNumber.text.toString() + "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                    processTransaction()
                } else {
                    Toast.makeText(this, getString(R.string.enter_valid_phone_amount), Toast.LENGTH_LONG).show()
                }
            }
            FALSE -> {
                if (amount.text.toString().isNotEmpty()) {
                    message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
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
                    message = "\nPhone : " + phoneNumber.text.toString() + "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                    processTransaction()
                } else {
                    Toast.makeText(this, getString(R.string.enter_valid_phone), Toast.LENGTH_LONG).show()
                }
            }
            FALSE -> {
                message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
                processTransaction()

            }

        }
    }

    private fun validateTransfer() {
        if (accountNumber.text.toString().isNotEmpty() && amount.text.toString().isNotEmpty()) {
            processTransaction()
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

    private fun checkActionRequest(): HoverParameters.Builder {

        saveTransaction = saveTransactionSwt.isChecked
        phoneValue = phoneNumber.text.toString()
        prefManager.simOSReportedHni?.let {
            simOSReportedHniValue = it
        }

        when (transactionType) {

            AIRTIME -> when (othersSwt.isChecked) {

                //check the param from the steps from api response and add the extras to the hover parameters

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

                //check the param from the steps from api response and add the extras to the hover parameters

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
                            .setHeader(transaction).request(prefManager.dataSelfAction)
                }
            }
            TRANSFER -> {

            }
        }

        return hoverParameters
    }

    private fun checkSwitch() {

        othersSwt.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                othersSwt.text = getString(R.string.others)
            } else {
                othersSwt.text = getString(R.string.self)
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

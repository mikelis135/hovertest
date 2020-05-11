package com.usehover.hovertest.transaction

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.hover.sdk.api.HoverParameters
import com.usehover.hovertest.HoverApp
import com.usehover.hovertest.R
import com.usehover.hovertest.di.ViewModelFactory
import com.usehover.hovertest.model.TransactionTypes
import com.usehover.hovertest.model.TransactionTypes.Data
import kotlinx.android.synthetic.main.activity_new_transaction.*
import javax.inject.Inject

class NewTransactionActivity : AppCompatActivity(), TextView.OnEditorActionListener {

    private val TAG = "NewTransactionActivity"
    private var message = ""
    private var advertMessage = ""
    private var transaction = ""
    private var simOSReportedHniValue = ""
    private var amountValue = ""
    private var accountNumberValue = ""
    private var dataOptionValue = ""
    private var accountOptionValue = ""
    private var phoneValue = ""
    private var saveTransaction = false


    private var transferSelfAction = ""
    private var airtimeOthersAction = ""
    private var airtimeSelfAction = ""
    private var dataOthersAction = ""
    private var dataSelfAction = ""
    private var transferOthersAction = ""
    private var dataBundleAction = ""
    private var accountResolveAction = ""


    private var bundleOption = arrayListOf<String>()
    private var bundleValue = arrayListOf<String>()
    private var accountOption = arrayListOf<String>()
    private var accountValue = arrayListOf<String>()

    private lateinit var hoverParameters: HoverParameters.Builder
    private var transactionType: TransactionTypes? = null
    private var lastClickTime = 0L

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: NewTransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as HoverApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(NewTransactionViewModel::class.java)

        setContentView(R.layout.activity_new_transaction)

        setSupportActionBar(toolbar)

        checkTransactionType()

        viewModel.setUpTransaction(transactionType)

        checkSwitch()

        accountNumberCheck()

        phoneNumberCheck()

        amount.setOnEditorActionListener(this)

        repeatTransfer()

        payOfflineBtn.setOnClickListener {
            hoverParameters = HoverParameters.Builder(this)
            pay()
        }

        backImg.setOnClickListener {
            finish()
        }

        setUpObservers()

    }

    override fun onEditorAction(textview: TextView?, actionId: Int, event: KeyEvent?): Boolean {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            textview?.let {
                val imm = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
                hoverParameters = HoverParameters.Builder(this)
                pay()
                return true
            }

        }

        return false
    }

    private fun pay() {
        singleClick()
        message = "\nTransaction : " + transactionTypeSP.selectedItem.toString() + "\nAmount :  " + "₦" + amount.text.toString() + "\n\n"
        var bundle = ""
        try {
            bundle = bundleValue[transactionTypeSP.selectedItemPosition]
        } catch (ignore: java.lang.Exception) {

        }
        amountValue = amount.text.toString()
        phoneValue = phoneNumber.text.toString()
        accountNumberValue = accountNumber.text.toString()

        viewModel.validateTransaction(transactionType, amountValue, phoneValue, message, accountNumberValue, accountOptionValue, transaction, bundle, othersSwt.isChecked, saveTransactionSwt.isChecked)
    }

    private fun setUpObservers() {

        viewModel.toastLd.observe(this, Observer {

            it?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.advertMessageLd.observe(this, Observer {

            it?.getContentIfNotHandled()?.let {
                advertMessage = it
            }
        })

        viewModel.transactionTypeLd.observe(this, Observer {

            it?.let {
                transactionType = it
            }
        })

        viewModel.othersSwitchLd.observe(this, Observer {

            it?.let {
                othersSwt.text = it
            }
        })

        viewModel.buttonMessageLd.observe(this, Observer {

            it?.let {
                payOfflineBtn.text = it
            }
        })

        viewModel.animatePhoneLd.observe(this, Observer {

            it?.let {
                animate(it, phoneNumberLayout)
            }
        })

        viewModel.showBankLayoutLd.observe(this, Observer {

            it?.let {
                if (it) {
                    bankLayout.visibility = View.VISIBLE
                } else {
                    bankLayout.visibility = View.GONE
                }
            }
        })

        viewModel.transactionLd.observe(this, Observer {

            it?.let {
                transaction = it
            }
        })

        viewModel.setUpAirtimeLd.observe(this, Observer {

            it?.let {
                if (it) {
                    setUpAirtime()
                }
            }
        })

        viewModel.setUpDataLd.observe(this, Observer {

            it?.let {
                if (it) {
                    setUpData()
                }
            }
        })

        viewModel.setUpTransferLd.observe(this, Observer {

            it?.let {
                if (it) {
                    setUpTransfer()
                }
            }
        })

        viewModel.processAirtimeforSelfLd.observe(this, Observer {

            it?.let {
                if (it) {
                    startTransaction(prepareAirtimeForSelf())
                } else {
                    startTransaction(prepareAirtimeForOthers())
                }
            }
        })

        viewModel.processDataforSelfLd.observe(this, Observer {

            it?.let {
                if (it) {
                    startTransaction(prepareDataForSelf())
                } else {
                    startTransaction(prepareDataForOthers())
                }
            }
        })

        viewModel.processTransferforSameBankLd.observe(this, Observer {

            it?.let {
                if (it) {
                    startTransaction(prepareTransferForSameBank())
                } else {
                    startTransaction(prepareTransferForOtherBanks())
                }
            }
        })

        viewModel.processDataBundleRequestForSelfLd.observe(this, Observer {

            it?.let {
                if (it) {
                    startTransaction(prepareDataBundleForSelf())


                } else {
                    startTransaction(prepareDataBundleForOthers())
                }

            }
        })

        viewModel.processAccountResolveRequestLd.observe(this, Observer {

            it?.let {
                if (it) {
                    startTransaction(prepareAccountResolve())
                }
            }
        })

        viewModel.saveTransactionLd.observe(this, Observer {

            it?.let {
                if (it) {
                    saveTransaction = it
                }
            }
        })

        viewModel.messageLd.observe(this, Observer {

            it?.let {
                message = it
            }
        })

        viewModel.amountValueLd.observe(this, Observer {

            it?.let {
                amountValue = it
            }
        })

        viewModel.accountNumberValueLd.observe(this, Observer {

            it?.let {
                accountNumberValue = it
            }
        })

        viewModel.phoneValueLd.observe(this, Observer {

            it?.let {
                phoneValue = it
            }
        })

        viewModel.simOSReportedHniValueLd.observe(this, Observer {

            it?.let {
                simOSReportedHniValue = it
            }
        })


        viewModel.bundleValueLd.observe(this, Observer {

            it?.let {
                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, it)
                dataBundleSP.adapter = arrayAdapter
                dataBundleLayout.visibility = VISIBLE
            }
        })


        viewModel.accountValueLd.observe(this, Observer {

            it?.let {
                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, it)
                bankSP.adapter = arrayAdapter
                bankLayout.visibility = View.VISIBLE
            }
        })

    }

    private fun startTransaction(hoverParameters: HoverParameters.Builder) {
        try {
            val intent = hoverParameters.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun prepareAirtimeForSelf(): HoverParameters.Builder {

        viewModel.getAirtimeSelfAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .style(R.style.AppTheme)
                    .extra("amount", amountValue)
        }

        return hoverParameters
    }

    private fun prepareAirtimeForOthers(): HoverParameters.Builder {

        viewModel.getAirtimeOthersAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("phone", phoneValue)
                    .extra("amount", amountValue)
        }


        return hoverParameters
    }

    private fun prepareDataForSelf(): HoverParameters.Builder {

        viewModel.getDataSelfAction()?.let {
            dataOptionValue = bundleOption[dataBundleSP.selectedItemPosition]
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("option", dataOptionValue)
        }

        return hoverParameters
    }

    private fun prepareDataForOthers(): HoverParameters.Builder {

        viewModel.getDataOthersAction()?.let {
            dataOptionValue = bundleOption[dataBundleSP.selectedItemPosition]
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("phone", phoneValue)
                    .extra("option", dataOptionValue)
        }

        return hoverParameters
    }

    private fun prepareTransferForSameBank(): HoverParameters.Builder {

        viewModel.getTransferSelfAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .style(R.style.AppTheme)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("amount", amountValue)
                    .extra("account", accountNumberValue)
        }

        return hoverParameters
    }

    private fun prepareTransferForOtherBanks(): HoverParameters.Builder {

        viewModel.getTransferOthersAction()?.let {
            accountOptionValue = accountOption[bankSP.selectedItemPosition]
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("amount", amountValue)
                    .extra("account", accountNumberValue)
                    .extra("option", accountOptionValue)
        }

        return hoverParameters
    }

    private fun prepareDataBundleForSelf(): HoverParameters.Builder {

        viewModel.getDataBundleAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(dataBundleAction)
                    .style(R.style.AppTheme)
        }
        return hoverParameters
    }

    private fun prepareDataBundleForOthers(): HoverParameters.Builder {

        viewModel.getDataBundleAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("phone", phoneValue)
        }

        return hoverParameters
    }

    private fun prepareAccountResolve(): HoverParameters.Builder {

        viewModel.getAccountResolveAction()?.let {
            hoverParameters = HoverParameters.Builder(this@NewTransactionActivity)
                    .setSim(viewModel.getSimOSReportedHniValue())
                    .initialProcessingMessage(message + advertMessage)
                    .setHeader(transaction).request(it)
                    .style(R.style.AppTheme)
                    .extra("amount", amountValue)
                    .extra("account", accountNumberValue)
        }

        return hoverParameters
    }

//    private fun getDataBundles(isDataForSelf: Boolean) {
//        singleClick()
//        message = "\nProcessing...\n\n"
//        transaction = "\nGetting Data Bundle"
//        saveTransaction = saveTransactionSwt.isChecked
//        phoneValue = phoneNumber.text.toString()
//        view
//        simOSReportedHni?.let {
//            simOSReportedHniValue = it
//        }
//
//        viewModel.checkDataBundleRequest(phoneValue, isDataForSelf, saveTransactionSwt.isChecked)

//    }

    private fun singleClick() {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        viewModel.setUpActions()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        data?.let {

            if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

                clearAccountResolve()
                clearDataBundles()

                val sessionTextArr = it.getStringArrayExtra("session_messages")

                sessionTextArr?.let {
                    viewModel.processResult(transaction, it)
                }

                if (saveTransaction && !transaction.contains("data bundle", true) && !transaction.contains("account resolve", true)) {

                    viewModel.saveTransaction(transactionType, "₦$amountValue", phoneValue, message, othersSwt.isChecked, dataOptionValue, accountOptionValue, accountNumberValue, simOSReportedHniValue)

                } else {

//                    viewModel.saveTransaction(transactionType, amountValue, phoneValue, message, othersSwt.isChecked, dataOptionValue, accountOptionValue, accountNumberValue, simOSReportedHniValue)
                }
            }
        }

    }

    private fun checkTransactionType() {

        transactionTypeSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                viewModel.checkTransactionType(transactionTypeSP.selectedItem.toString(), othersSwt.isChecked)
            }

        }

    }

    private fun checkSwitch() {

        othersSwt.setOnCheckedChangeListener { _, isChecked ->

            viewModel.checkSwitch(transactionTypeSP.selectedItem.toString(), isChecked, accountOptionValue, transactionType)

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

    private fun setUpAirtime() {
        dataBundleLayout.visibility = GONE
        accountNumberLayout.visibility = GONE
        bankLayout.visibility = GONE
        phoneVisible(othersSwt.isChecked)
        animate(true, amountLayout)
        animate(false, bankLayout)
    }

    private fun setUpData() {
        clearDataBundles()
        accountNumberLayout.visibility = GONE
        amountLayout.visibility = GONE
        bankLayout.visibility = GONE
        dataBundleLayout.visibility = GONE
        phoneVisible(othersSwt.isChecked)
        animate(false, bankLayout)
    }

    private fun setUpTransfer() {
        clearAccountResolve()
        dataBundleLayout.visibility = GONE
        phoneNumberLayout.visibility = GONE
        bankLayout.visibility = GONE
        animate(true, amountLayout)
        animate(true, accountNumberLayout)
    }

    private fun animate(show: Boolean, layout: ConstraintLayout) {

        val transition = Fade()
        transition.duration = 400L
        transition.addTarget(layout)
        transition.addTarget(saveTransactionSwt)

        TransitionManager.beginDelayedTransition(viewGroup, transition)

        if (show) {
            layout.visibility = VISIBLE
        } else {
            layout.visibility = GONE
        }
    }

    private fun phoneVisible(otherPhones: Boolean) {
        if (otherPhones) {
            animate(true, phoneNumberLayout)
        } else {
            animate(false, phoneNumberLayout)
        }
    }

    private fun accountNumberCheck() {

        accountNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (othersSwt.isChecked) {
                    clearAccountResolve()
                    bankLayout.visibility = GONE
                    payOfflineBtn.text = getString(R.string.verify_account)
                }
            }

        })
    }

    private fun phoneNumberCheck() {

        phoneNumber.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (transactionType == Data) {
                    clearDataBundles()
                    dataBundleLayout.visibility = GONE
                    payOfflineBtn.text = getString(R.string.get_data_bundle)
                }
            }

        })
    }

    private fun clearAccountResolve() {
        accountOption.clear()
        accountValue.clear()
    }

    private fun clearDataBundles() {
        bundleOption.clear()
        bundleValue.clear()
    }

    private fun repeatTransfer() {

        intent?.let {
            val accountString = it.getStringExtra("account")
            val amountString = it.getStringExtra("amount")

            accountString?.let {
                accountNumber.setText(it)
            }

            amountString?.let {
                amount.setText(it)
            }

            transactionTypeSP.setSelection(2)
            othersSwt.isChecked = true
        }
    }

}

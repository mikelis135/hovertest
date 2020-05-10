package com.usehover.hovertest.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.R
import com.usehover.hovertest.event.OnTransactionSelectedListener
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes.*
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.store.PrefManager
import com.usehover.hovertest.transaction.NewTransactionActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.*

class HomeActivity : AppCompatActivity(), Hover.DownloadListener {


    private var homeAdapter: HomeAdapter = HomeAdapter(arrayListOf(), null)

    private lateinit var prefManager: PrefManager
    private lateinit var hoverParameters: HoverParameters.Builder
    private val simName = arrayListOf<String>()
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var textToSpeech: TextToSpeech
    private var advertMessage = ""
    private var transactionValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        prefManager = PrefManager(this)

        //  addSampleTransaction()

        recyclerSetup()

        setupVoice()

        setUpActions()

        setUpDialog()

        Hover.initialize(this, this@HomeActivity)

        checkPermissionAccepted()

        welcomeCheck()

        populateRecycler()

        newTransactionFab.setOnClickListener {
            say("New transaction")
            if (prefManager.fetchSim().isNullOrEmpty()) {
                startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
            } else {
                startActivity(Intent(this, NewTransactionActivity::class.java))
            }
        }
    }

    private fun recyclerSetup() {
        transactionRecycler.layoutManager = LinearLayoutManager(this)
    }

    //delete
    private fun addSampleTransaction() {
        prefManager.saveTransaction(Transaction(Airtime, "₦400", "08023838292", "Airtime hey", true, "", "", "", ""))
        prefManager.saveTransaction(Transaction(Data, "₦100 MB", "08023838292", "Data Hey", false, "", "", "", ""))
        prefManager.saveTransaction(Transaction(Transfer, "₦40, 000", "08023838292", "Transfer Hey", true, "", "", "", ""))

    }

    private fun populateRecycler() {

        if (!prefManager.fetchTransactions().isNullOrEmpty()) {

            welcomeTxt.visibility = View.GONE

            prefManager.fetchTransactions()?.let {

                homeAdapter = HomeAdapter(it, object : OnTransactionSelectedListener {

                    override fun onTransactionDelete(position: Int) {

                        showDeleteDialog(position)
                    }

                    override fun onTransactionSelected(transaction: Transaction) {

                        selectTransaction(transaction)
                    }

                })

                transactionRecycler.adapter = homeAdapter
            }

        } else {
            welcomeTxt.visibility = View.VISIBLE
        }

    }

    private fun showDeleteDialog(position: Int) {

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            homeAdapter.let {
                val newTransactionList = it.removeTransaction(position)
                prefManager.saveTransactions(newTransactionList)
                if (newTransactionList.isEmpty()) {
                    welcomeTxt.visibility = View.VISIBLE
                }
            }

        }
        alertDialogBuilder.show()
    }

    private fun setUpDialog() {
        alertDialogBuilder = AlertDialog.Builder(this@HomeActivity, R.style.dialogStyle)
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setMessage(getString(R.string.delete_saved_transaction_prompt))
        alertDialogBuilder.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
    }

    private fun setupVoice() {
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.apply {
                    language = Locale.UK
                    setPitch(0.6f)
                }
                say(getString(R.string.welcome_note))
            } else {
                Log.d("okh", "Oops, i have a bad voice at the moment")
            }

        })

    }

    private fun say(whatToSay: String) {
        if (prefManager.voiceEnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(whatToSay, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
                textToSpeech.speak(whatToSay, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    override fun finish() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.finish()
    }

    private fun selectTransaction(transaction: Transaction) {

        try {

            transactionValue = when {
                transaction.transactionType.toString().contains(Airtime.name, true) -> getString(R.string.buying_airtime)
                transaction.transactionType.toString().contains(Data.name, true) -> getString(R.string.buying_data)
                transaction.transactionType.toString().contains(Transfer.name, true) -> getString(R.string.sending_money)
                else -> getString(R.string.empty)
            }

            hoverParameters = checkActionRequest(transaction)
            val intent = hoverParameters.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {

        }
    }

    private fun checkActionRequest(transaction: Transaction): HoverParameters.Builder {

        val amount = transaction.amount.replace("₦", "")

        when (transaction.transactionType) {


            Airtime -> when (transaction.isOthers) {

                TRUE -> {

                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.airtimeOthersAction)
                            .extra("phone", transaction.phone)
                            .extra("amount", amount)
                }

                FALSE -> {
                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.airtimeSelfAction)
                            .style(R.style.AppTheme)
                            .extra("amount", amount)

                }
            }

            Data -> when (transaction.isOthers) {

                TRUE -> {

                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.dataOthersAction)
                            .style(R.style.AppTheme)
                            .extra("phone", transaction.phone)
                            .extra("option", transaction.dataOptionValue)

                }

                FALSE -> {
                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .style(R.style.AppTheme)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.dataSelfAction)
                            .style(R.style.AppTheme)
                            .extra("option", transaction.dataOptionValue)
                }

            }

            Transfer -> when (transaction.isOthers) {

                TRUE -> {

                    val intent = Intent(this, NewTransactionActivity::class.java)
                    intent.putExtra("account", transaction.accountNumberValue)
                    intent.putExtra("amount", transaction.amount)
                    startActivity(intent)
                }

                FALSE -> {
                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.transferSelfAction)
                            .style(R.style.AppTheme)
                            .extra("amount", transaction.amount)
                            .extra("account", transaction.accountNumberValue)
                }

            }

            Balance -> {

                hoverParameters = HoverParameters.Builder(this)
                        .setSim(transaction.simOSReportedHni)
                        .initialProcessingMessage(transaction.message + advertMessage)
                        .setHeader(transactionValue).request(prefManager.accountBalanceAction)
                        .style(R.style.AppTheme)
            }

        }

        return hoverParameters
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

    override fun onSuccess(actions: ArrayList<HoverAction>) {

        if (prefManager.fetchActions().isNullOrEmpty()) {
            welcomeTxt.text = getString(R.string.setup_done)
            setupBtn.text = getString(R.string.profile)
            actionsPb.visibility = View.GONE
        }

        prefManager.saveActions(actions)
        Log.d("okh", "Successfully downloaded " + actions.size + " actions")

    }

    override fun onError(error: String?) {

        setupBtn.setOnClickListener {
            if (prefManager.fetchActions().isNullOrEmpty()) {
                actionsPb.visibility = View.VISIBLE
                Hover.initialize(this, this@HomeActivity)
            }
        }

    }

    override fun onResume() {

        populateRecycler()

        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.user_profile, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.profile -> {
                say("Set Profile here")
                saveSimDetails()
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.balance -> {

                if (!prefManager.fetchActions().isNullOrEmpty()) {
                    say("Account balance...please enter your pin")
                    val transaction = Transaction(Balance)
                    prefManager.simOSReportedHni?.let {
                        transaction.simOSReportedHni = it
                    }
                    transaction.message = advertMessage
                    transactionValue = getString(R.string.account_balance)
                    try {
                        val hoverParameters = checkActionRequest(transaction)
                        val intent = hoverParameters.buildIntent()
                        startActivityForResult(intent, 0)
                    } catch (ignore: java.lang.Exception) {

                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun checkPermissionAccepted() {

        if (prefManager.fetchSim().isNullOrEmpty()) {
            welcomeTxt.text = getString(R.string.permission_note)
            setupBtn.visibility = View.VISIBLE
            newTransactionFab.visibility = View.GONE
            startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
            saveSimDetails()
            setupBank()
        } else {
            welcomeTxt.text = getString(R.string.welcome_note)
            setupBtn.visibility = View.GONE
            newTransactionFab.visibility = View.VISIBLE
        }
    }

    private fun saveSimDetails() {

        val simList = Hover.getPresentSims(this@HomeActivity)
        simName.clear()

        if (!simList.isNullOrEmpty()) {
            simList.forEach {
                simName.add(it.networkOperatorName)
            }

            prefManager.saveSim(simList)

            if (prefManager.simOSReportedHni.isNullOrEmpty()) {
                prefManager.simOSReportedHni = simList[0].osReportedHni
            }

        }

    }

    private fun setupBank() {
        val bankArray = resources.getStringArray(R.array.bank)
        prefManager.bankName = bankArray[0]

    }

    private fun setupView() {

        if (prefManager.fetchActions().isNullOrEmpty()) {

            welcomeTxt.text = getString(R.string.internet_needed)
            say(getString(R.string.internet_needed))
            newTransactionFab.visibility = View.GONE
            setupBtn.visibility = View.VISIBLE

        } else {
            welcomeTxt.text = getString(R.string.welcome_note)
            say(getString(R.string.welcome_note))
            newTransactionFab.visibility = View.VISIBLE
            setupBtn.visibility = View.GONE
        }
    }

    private fun welcomeCheck() {

        setupView()

        setupBtn.setOnClickListener {

            if (prefManager.fetchActions().isNullOrEmpty()) {
                Hover.initialize(this, this@HomeActivity)
                actionsPb.visibility = View.VISIBLE

            } else {
                checkPermissionAccepted()
                saveSimDetails()
                setupBank()
                startActivity(Intent(this, ProfileActivity::class.java))
            }

        }

    }

}

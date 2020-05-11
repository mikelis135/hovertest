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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.usehover.hovertest.HoverApp
import com.usehover.hovertest.R
import com.usehover.hovertest.di.ViewModelFactory
import com.usehover.hovertest.event.OnTransactionSelectedListener
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes.Balance
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.transaction.NewTransactionActivity
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import javax.inject.Inject

@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity(), Hover.DownloadListener {


    private var homeAdapter: HomeAdapter = HomeAdapter(arrayListOf(), null)

    private lateinit var hoverParameters: HoverParameters.Builder
    private val simName = arrayListOf<String>()
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var textToSpeech: TextToSpeech
    private var advertMessage = ""
    private var transactionValue = ""
    private var amount = ""


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: HomeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as HoverApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(HomeViewModel::class.java)

        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        //  addSampleTransaction()

        recyclerSetup()

        setupVoice()

        setUpDialog()

        Hover.initialize(this, this@HomeActivity)

        checkPermissionAccepted()

        welcomeCheck()

        populateRecycler()

        newTransactionFab.setOnClickListener {
            say("New transaction")

            viewModel.fetchSim(false)
        }

        setUpObservers()
    }

    private fun setUpObservers() {

        viewModel.simLd.observe(this, Observer {
            it?.getContentIfNotHandled()?.let {
                if (!it) {
                    startActivity(Intent(this, NewTransactionActivity::class.java))
                }
                viewModel.simLd.value = null
            }

        })

        viewModel.processAirtimeForSelfLd.observe(this, Observer {

            it?.let {
                it.forEach {
                    if (it.key) {
                        startTransaction(prepareAirtimeForSelf(it.value))
                    } else {
                        startTransaction(prepareAirtimeForOthers(it.value))
                    }
                }
            }
        })

        viewModel.processDataForSelfLd.observe(this, Observer {

            it?.let {
                it.forEach {
                    if (it.key) {
                        startTransaction(prepareDataForSelf(it.value))
                    } else {
                        startTransaction(prepareDataForOthers(it.value))
                    }
                }

            }
        })

        viewModel.processTransferForSameBankLd.observe(this, Observer {

            it?.let {
                it.forEach {
                    if (it.key) {
                        startTransaction(prepareTransferForSameBank(it.value))
                    } else {
                        startTransaction(prepareTransferForOtherBanks(it.value))
                    }
                }
            }
        })

        viewModel.processBalanceLd.observe(this, Observer {

            it?.let {
                it.forEach {
                    if (it.key) {
                        startTransaction(prepareBalance(it.value))
                    }
                }
            }
        })

        viewModel.amountLd.observe(this, Observer {

            it?.let {
                amount = it
            }
        })


        viewModel.showWelcomeLd.observe(this, Observer {

            it?.let {
                if (!it) {
                    welcomeTxt.visibility = View.GONE
                } else {
                    welcomeTxt.visibility = View.VISIBLE
                }

            }
        })

        viewModel.amountLd.observe(this, Observer {

            it?.let {
                advertMessage = it
            }
        })

        viewModel.voiceLd.observe(this, Observer {

            it?.let {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech.speak(it, TextToSpeech.QUEUE_FLUSH, null)
                }

            }
        })

        viewModel.transactionListLd.observe(this, Observer {

            it?.let {
                it.let {

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
            }
        })


        viewModel.actionsLd.observe(this, Observer {

            it?.let {
                if (it) {
                    welcomeTxt.text = getString(R.string.welcome_note)
                    setupBtn.visibility = View.GONE
                    newTransactionFab.visibility = View.VISIBLE
                    startActivity(Intent(this, ProfileActivity::class.java))
                } else {
                    actionsPb.visibility = View.VISIBLE
                    Hover.initialize(this)
                }

            }
        })

        viewModel.getBalanceLd.observe(this, Observer {

            it?.let {

                say("Account balance...please enter your pin")
                val transaction = Transaction(Balance)
                transaction.simOSReportedHni = it
                transaction.message = advertMessage
                transactionValue = getString(R.string.account_balance)
                try {
                    viewModel.checkAction(transaction)
                    val intent = hoverParameters.buildIntent()
                    startActivityForResult(intent, 0)
                } catch (ignore: java.lang.Exception) {

                }

            }
        })

        viewModel.setUpLd.observe(this, Observer {

            it?.let {
                if (it) {
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
        })


        viewModel.setPermissionLd.observe(this, Observer {

            it?.let {
                if (it) {
                    newTransactionFab.visibility = View.GONE
                    setupBtn.visibility = View.VISIBLE
                } else {
                    setupBtn.visibility = View.GONE
                    newTransactionFab.visibility = View.VISIBLE
                }
            }
        })


        viewModel.welcomeCheckLd.observe(this, Observer {

            it?.let {
                if (it) {
                    Hover.initialize(this, this@HomeActivity)
                    actionsPb.visibility = View.VISIBLE
                } else {
                    checkPermissionAccepted()
                    saveSimDetails()
                    setupBank()
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
            }
        })


    }

    private fun prepareBalance(transaction: Transaction): HoverParameters.Builder {

        viewModel.getAccountBalanceAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
        }


        return hoverParameters
    }

    private fun prepareTransferForOtherBanks(transaction: Transaction): HoverParameters.Builder {
        hoverParameters = HoverParameters.Builder(this)
        val intent = Intent(this, NewTransactionActivity::class.java)
        intent.putExtra("account", transaction.accountNumberValue)
        intent.putExtra("amount", amount)
        startActivity(intent)

        return hoverParameters
    }

    private fun prepareTransferForSameBank(transaction: Transaction): HoverParameters.Builder {

        viewModel.getTransferSelfAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
                    .extra("amount", amount)
                    .extra("account", transaction.accountNumberValue)
        }


        return hoverParameters
    }

    private fun prepareDataForOthers(transaction: Transaction): HoverParameters.Builder {

        viewModel.getDataOthersAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
                    .extra("phone", transaction.phone)
                    .extra("option", transaction.dataOptionValue)
        }

        return hoverParameters
    }

    private fun prepareDataForSelf(transaction: Transaction): HoverParameters.Builder {

        viewModel.getDataOthersAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .style(R.style.AppTheme)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
                    .extra("option", transaction.dataOptionValue)
        }


        return hoverParameters
    }

    private fun prepareAirtimeForOthers(transaction: Transaction): HoverParameters.Builder {

        viewModel.getAirtimeOthersAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
                    .extra("phone", transaction.phone)
                    .extra("amount", amount)
        }

        return hoverParameters
    }

    private fun prepareAirtimeForSelf(transaction: Transaction): HoverParameters.Builder {

        viewModel.getAirtimeSelfAction()?.let {
            hoverParameters = HoverParameters.Builder(this)
                    .setSim(transaction.simOSReportedHni)
                    .initialProcessingMessage(transaction.message + advertMessage)
                    .setHeader(transactionValue).request(it)
                    .style(R.style.AppTheme)
                    .extra("amount", amount)
        }

        return hoverParameters
    }

    private fun recyclerSetup() {
        transactionRecycler.layoutManager = LinearLayoutManager(this)
    }

    //delete
//    private fun addSampleTransaction() {
//        saveTransaction(Transaction(Airtime, "₦400", "08023838292", "Airtime hey", true, "", "", "", ""))
//        saveTransaction(Transaction(Data, "₦100 MB", "08023838292", "Data Hey", false, "", "", "", ""))
//        saveTransaction(Transaction(Transfer, "₦40, 000", "08023838292", "Transfer Hey", true, "", "", "", ""))
//
//    }

    private fun populateRecycler() {

        viewModel.fetchTransactions()


    }

    private fun showDeleteDialog(position: Int) {

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            homeAdapter.let {

                val newTransactionList = it.removeTransaction(position)
                viewModel.saveTransaction(newTransactionList)

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
        viewModel.checkVoice(whatToSay)
    }

    override fun finish() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.finish()
    }

    private fun selectTransaction(transaction: Transaction) {

        viewModel.setTransaction(transaction.transactionType.toString())
        viewModel.checkAction(transaction)

    }

    private fun startTransaction(hoverParameters: HoverParameters.Builder) {
        try {
            val intent = hoverParameters.buildIntent()
            startActivityForResult(intent, 0)
        } catch (e: Exception) {

        }
    }

    private fun setUpActions() {

        viewModel.setUpAction()


    }

    override fun onSuccess(actions: ArrayList<HoverAction>) {

        viewModel.fetchActions(false)
        viewModel.saveActions(actions)
        Log.d("okh", "Successfully downloaded " + actions.size + " actions")

    }

    override fun onError(error: String?) {

        setupBtn.setOnClickListener {
            viewModel.fetchActions(true)
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

                viewModel.fetchActions(false, true)

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun checkPermissionAccepted() {

        viewModel.fetchSim(true)
    }

    private fun saveSimDetails() {

        val simList = Hover.getPresentSims(this@HomeActivity)
        simName.clear()

        if (!simList.isNullOrEmpty()) {
            simList.forEach {
                simName.add(it.networkOperatorName)
            }

            viewModel.saveSim(simList)

        }

    }

    private fun setupBank() {
        val bankArray = resources.getStringArray(R.array.bank)
        viewModel.saveBankName(bankArray[0])

    }

    private fun setupView() {

        viewModel.setUp()
    }

    private fun welcomeCheck() {

        setupView()

        setupBtn.setOnClickListener {

            viewModel.fetchActions(false, false, true)

        }

    }

}

package com.usehover.hovertest.home

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.api.HoverParameters
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.*
import com.usehover.hovertest.event.OnTransactionSelectedListener
import com.usehover.hovertest.profile.ProfileActivity
import com.usehover.hovertest.store.PrefManager
import com.usehover.hovertest.transaction.NewTransactionActivity
import com.usehover.hovertest.model.Transaction
import com.usehover.hovertest.model.TransactionTypes
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.toolbar
import kotlinx.android.synthetic.main.profile_activity.*
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.util.ArrayList

class HomeActivity : AppCompatActivity(), Hover.DownloadListener {


    private var homeAdapter: HomeAdapter = HomeAdapter(arrayListOf(), null)

    private lateinit var prefManager: PrefManager
    private lateinit var hoverParameters: HoverParameters.Builder
    val simName = arrayListOf<String>()

    private var advertMessage = ""
    private var transactionValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)

        recyclerSetup()

        prefManager = PrefManager(this)

        setUpActions()

        Hover.initialize(this, this@HomeActivity)

        checkPermissionAccepted()

        welcomeCheck()

        populateRecycler()

        newTransactionFab.setOnClickListener {
            if (prefManager.fetchSim().isNullOrEmpty()) {
                startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
            } else {
                startActivity(Intent(this, NewTransactionActivity::class.java))
            }
        }
    }

    private fun recyclerSetup() {
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val divider = ContextCompat.getDrawable(transactionRecycler.context, R.drawable.divider)
        val inset = resources.getDimensionPixelSize(R.dimen._10dp)
        val insetDivider = InsetDrawable(divider, inset, 0, inset, 0)
        itemDecoration.setDrawable(insetDivider)
        transactionRecycler.addItemDecoration(itemDecoration)
        transactionRecycler.layoutManager = LinearLayoutManager(this)
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
        val builder = AlertDialog.Builder(this@HomeActivity, R.style.dialogStyle)
        builder.setCancelable(true)
        builder.setMessage(getString(R.string.delete_saved_transaction_prompt))

        builder.setPositiveButton("Yes") { _, _ ->
            homeAdapter.let {
                val newTransactionList = it.removeTransaction(position)
                prefManager.saveTransactions(newTransactionList)
                if (newTransactionList.isEmpty()) {
                    welcomeTxt.visibility = View.VISIBLE
                }
            }

        }
        builder.setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun selectTransaction(transaction: Transaction) {

        try {

            transactionValue = when {
                transaction.transactionType.toString().contains(TransactionTypes.AIRTIME.name, true) -> getString(R.string.buying_airtime)
                transaction.transactionType.toString().contains(TransactionTypes.DATA.name, true) -> getString(R.string.buying_data)
                else -> getString(R.string.sending_money)
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


            TransactionTypes.AIRTIME -> when (transaction.isOthers) {

                //check the param from the steps from api response and add the extras to the hover parameters

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
                            .extra("amount", amount)

                }
            }

            TransactionTypes.DATA -> when (transaction.isOthers) {

                //check the param from the steps from api response and add the extras to the hover parameters

                TRUE -> {
                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.dataOthersAction)
                            .extra("phone", transaction.phone)

                }
                FALSE -> {
                    hoverParameters = HoverParameters.Builder(this)
                            .setSim(transaction.simOSReportedHni)
                            .style(R.style.AppTheme)
                            .initialProcessingMessage(transaction.message + advertMessage)
                            .setHeader(transactionValue).request(prefManager.dataSelfAction)
                }
            }
            TransactionTypes.TRANSFER -> {

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

        if (prefManager.fetchActions().isNullOrEmpty()) {

            setupBtn.setOnClickListener {
                actionsPb.visibility = View.VISIBLE
                Hover.initialize(this, this@HomeActivity)
            }
        }

    }

    override fun onResume() {

        checkPermissionDone()

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
                startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
                setupSim()
                startActivity(Intent(this, ProfileActivity::class.java))
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
            setupSim()
            setupBank()
        } else {
            welcomeTxt.text = getString(R.string.welcome_note)
            setupBtn.visibility = View.GONE
            newTransactionFab.visibility = View.VISIBLE
        }
    }

    private fun setupSim() {

        val simList = Hover.getPresentSims(this@HomeActivity)
        simName.clear()

        if (!simList.isNullOrEmpty()) {
            simList.forEach {
                simName.add(it.networkOperatorName)
            }
            prefManager.saveSim(simName)
            prefManager.simOSReportedHni = simList[0].osReportedHni

        }

    }

    private fun setupBank() {
        val bankArray = resources.getStringArray(R.array.bank)
        prefManager.bankName = bankArray[0]

    }

    private fun setupView() {

        if (prefManager.fetchActions().isNullOrEmpty()) {

            welcomeTxt.text = getString(R.string.internet_needed)
            newTransactionFab.visibility = View.GONE
            setupBtn.visibility = View.VISIBLE

        } else {

            welcomeTxt.text = getString(R.string.welcome_note)
            newTransactionFab.visibility = View.VISIBLE
            setupBtn.visibility = View.GONE
        }
    }

    private fun checkPermissionDone() {
        if (!prefManager.fetchSim().isNullOrEmpty()) {
            welcomeTxt.text = getString(R.string.welcome_note)
            setupBtn.visibility = View.GONE
            newTransactionFab.visibility = View.VISIBLE
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
                setupSim()
                setupBank()
                startActivity(Intent(this, ProfileActivity::class.java))
            }

        }

    }


}

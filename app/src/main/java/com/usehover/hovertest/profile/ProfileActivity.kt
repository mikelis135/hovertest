package com.usehover.hovertest.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.HoverApp
import com.usehover.hovertest.R
import com.usehover.hovertest.di.ViewModelFactory
import kotlinx.android.synthetic.main.profile_activity.*
import javax.inject.Inject

class ProfileActivity : AppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ProfileViewModel
    private var simOSReportedHni = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)


        (application as HoverApp).appComponent.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(ProfileViewModel::class.java)

        setupSim()

        backImg.setOnClickListener {
            saveProfile()
            finish()
        }

        voiceSwt.isChecked = viewModel.isVoiceEnabled()
        bankSP.setSelection(viewModel.getBankPosition())

        setupObservers()

    }

    private fun setupObservers() {

        viewModel.simLd.observe(this, Observer {
            it?.let {
                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, it)
                simSP.adapter = arrayAdapter
                simSP.setSelection(viewModel.getSimPosition())
            }
        })

        viewModel.simOSReportedHniLd.observe(this, Observer {
            it?.let {
                simOSReportedHni = it
            }
        })
    }

    override fun onPause() {

        saveProfile()

        super.onPause()
    }

    private fun saveProfile() {

        viewModel.saveVoice(voiceSwt.isChecked)
        viewModel.saveBankName(bankSP.selectedItem.toString())
        viewModel.saveBankPosition(bankSP.selectedItemPosition)
        viewModel.saveSimPosition(simSP.selectedItemPosition)
        viewModel.saveSimOSReportedHni(simOSReportedHni[simSP.selectedItemPosition])

    }

    private fun setupSim() {

        val simList = Hover.getPresentSims(this@ProfileActivity)
        if (simList.isNullOrEmpty()) {
            finish()
            startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
        } else {

            viewModel.fetchSim()

        }


    }

}
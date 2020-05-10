package com.usehover.hovertest.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.R
import com.usehover.hovertest.store.PrefManager
import kotlinx.android.synthetic.main.profile_activity.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private var simOSReportedHni = arrayListOf<String>()
    private var simName = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        prefManager = PrefManager(this)

        setupSim()

        backImg.setOnClickListener {
            saveProfile()
            finish()
        }

        simSP.setSelection(prefManager.simPosition)
        bankSP.setSelection(prefManager.bankPosition)
        voiceSwt.isChecked = prefManager.voiceEnable

    }

    override fun onPause() {

        saveProfile()

        super.onPause()
    }

    private fun saveProfile() {

        prefManager.voiceEnable = voiceSwt.isChecked
        prefManager.bankName = bankSP.selectedItem.toString()
        prefManager.bankPosition = bankSP.selectedItemPosition
        prefManager.simPosition = simSP.selectedItemPosition

        prefManager.simOSReportedHni?.let {
            if (simOSReportedHni.isNotEmpty()) {
                prefManager.simOSReportedHni = simOSReportedHni[simSP.selectedItemPosition]
            }
        }
    }

    private fun setupSim() {

        val simList = Hover.getPresentSims(this@ProfileActivity)
        if (simList.isNullOrEmpty()) {
            finish()
            startActivityForResult(Intent(applicationContext, PermissionActivity::class.java), 0)
        } else {
            prefManager.fetchSim()?.let {

                it.forEach {
                    simOSReportedHni.add(it.osReportedHni.toString())
                    simName.add(it.operatorName.toString())
                }
                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, simName)
                simSP.adapter = arrayAdapter
            }
        }


    }

}
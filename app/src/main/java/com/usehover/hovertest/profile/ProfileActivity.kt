package com.usehover.hovertest.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionActivity
import com.usehover.hovertest.store.PrefManager
import com.usehover.hovertest.R
import kotlinx.android.synthetic.main.profile_activity.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefManager: PrefManager
    private val simOSReportedHni = arrayListOf<String>()

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

    }

    override fun onPause() {

        saveProfile()

        super.onPause()
    }

    private fun saveProfile() {
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
                val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, it)
                simSP.adapter = arrayAdapter
            }
        }


    }

}
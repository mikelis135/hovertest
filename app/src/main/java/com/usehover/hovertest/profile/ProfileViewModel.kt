package com.usehover.hovertest.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.usehover.hovertest.store.PrefManager
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
        val prefManager: PrefManager
) : ViewModel() {

    val simLd: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val simOSReportedHniLd: MutableLiveData<ArrayList<String>> = MutableLiveData()

    private var simOSReportedHni = arrayListOf<String>()
    private var simName = arrayListOf<String>()

    fun getSimPosition(): Int {
        return prefManager.simPosition
    }

    fun getBankPosition(): Int {
        return prefManager.bankPosition
    }

    fun isVoiceEnabled(): Boolean {
        return prefManager.voiceEnable
    }

    fun saveVoice(isEnabled: Boolean) {
        prefManager.voiceEnable = isEnabled
    }

    fun saveBankName(bankName: String) {
        prefManager.bankName = bankName
    }

    fun saveBankPosition(bankPosition: Int) {
        prefManager.bankPosition = bankPosition
    }

    fun saveSimPosition(simPosition: Int) {
        prefManager.simPosition = simPosition
    }

    fun saveSimOSReportedHni(simOSReportedHni: String) {
        prefManager.simOSReportedHni?.let {
            if (simOSReportedHni.isNotEmpty()) {
                prefManager.simOSReportedHni = simOSReportedHni
            }
        }
    }

    fun fetchSim() {
        prefManager.fetchSim()?.let {

            it.forEach {
                simOSReportedHni.add(it.osReportedHni.toString())
                simName.add(it.operatorName.toString())
                simOSReportedHniLd.value = simOSReportedHni
            }

            simLd.value = simName
        }
    }

}
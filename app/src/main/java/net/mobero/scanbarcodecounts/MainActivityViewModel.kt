
package net.mobero.scanbarcodecounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mobero.scanbarcodecounts.result.ScanResult

class MainActivityViewModel: ViewModel() {
    val resultLiveData: MutableLiveData<List<ScanResult>> = MutableLiveData(emptyList())

    fun add(result: ScanResult): Boolean {
        val results = resultLiveData.value ?: emptyList()
        if (results.contains(result)) return false
        resultLiveData.value = results + result
        return true
    }
}
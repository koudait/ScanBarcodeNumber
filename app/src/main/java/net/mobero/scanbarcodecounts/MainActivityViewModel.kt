
package net.mobero.scanbarcodecounts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mobero.scanbarcodecounts.result.ScanResult

class MainActivityViewModel: ViewModel() {
    val resultLiveData: MutableLiveData<List<ScanResult>> = MutableLiveData(emptyList())

    fun add(result: ScanResult): Boolean {
        //リストを取得する
        val results = resultLiveData.value ?: emptyList()
        //取得したリストの中に存在した場合、何もしない。
        if (results.contains(result)) return false
        //なかった場合、リストに新規追加する
        resultLiveData.value = results + result
        return true
    }

    fun get(value: String) : ScanResult? {
        //リストを取得する
        val results = resultLiveData.value ?: emptyList()

        results.forEach{
            if(it.value == value){
                //取得したリストの中に同一のバーコード存在した場合、そのアイテムをかえす
                return it
            }
        }
        return null
    }
}

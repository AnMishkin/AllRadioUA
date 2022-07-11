package download.mishkindeveloper.AllRadioUA.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    private val loadingStatus: MutableLiveData<Boolean> = MutableLiveData()

    @JvmName("getLoadingStatus1")
    fun getLoadingStatus(): MutableLiveData<Boolean>? {
        return loadingStatus
    }
}
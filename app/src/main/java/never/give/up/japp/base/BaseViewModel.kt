package never.give.up.japp.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import never.give.up.japp.consts.State
import never.give.up.japp.data.JAppDatabase
import never.give.up.japp.net.ApiService
import never.give.up.japp.net.RetrofitClient

/**
 * @By Journey 2020/12/2
 * @Description
 */
open class BaseViewModel:ViewModel() {
    protected val db by lazy { JAppDatabase.getInstance() }
    val loadStatus by lazy { MutableLiveData<State>() }
    protected val apiService: ApiService by lazy {
        RetrofitClient.instance.apiService
    }
    protected val singerApiService: ApiService by lazy {
        RetrofitClient.instance.singerApiService
    }
    protected val songUrlApiService: ApiService by lazy {
        RetrofitClient.instance.songUrlApiService
    }
    protected val neteaseApiService: ApiService by lazy {
        RetrofitClient.instance.neteaseApiService
    }
    protected fun request(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block.invoke()
            } catch (e:Exception) {

            }
        }
    }
}
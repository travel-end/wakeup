package never.give.up.japp.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import never.give.up.japp.consts.State
import never.give.up.japp.data.JAppDatabase

/**
 * @By Journey 2020/12/2
 * @Description
 */
open class BaseViewModel:ViewModel() {
    protected val db by lazy { JAppDatabase.getInstance() }
    val loadStatus by lazy { MutableLiveData<State>() }
    protected fun request(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block.invoke()
            } catch (e:Exception) {

            }
        }
    }
}
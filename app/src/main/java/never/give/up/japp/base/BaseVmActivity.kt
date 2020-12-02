package never.give.up.japp.base

import androidx.lifecycle.ViewModelProvider
import never.give.up.japp.utils.getClass


/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class BaseVmActivity<VM:BaseViewModel>:BaseActivity() {
    protected lateinit var mViewModel: VM
    override fun initViewModel() {
        super.initViewModel()
        mViewModel = ViewModelProvider(this,BaseViewModelFactory())[getClass(this)]
    }
}
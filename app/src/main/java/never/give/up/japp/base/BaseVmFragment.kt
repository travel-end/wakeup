package never.give.up.japp.base

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import never.give.up.japp.consts.State
import never.give.up.japp.consts.StateType
import never.give.up.japp.utils.getClass

/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class BaseVmFragment<VM:BaseViewModel>:BaseFragment() {
    protected lateinit var mViewModel: VM
    override fun initViewModel() {
        super.initViewModel()
        mViewModel = ViewModelProvider(this, BaseViewModelFactory())[getClass(this)]
    }

    override fun initData() {
        super.initData()
        observe()
    }
    open fun observe() {
        statusObserve()
    }
    private fun statusObserve() {
        mViewModel.loadStatus.observe(this, loadStatusObserver)
    }

    private val loadStatusObserver by lazy {
        Observer<State> {
            it?.let {
                when (it.code) {
                    StateType.SUCCESS -> {}
                    StateType.ERROR -> {}
                    StateType.LOADING_NORMAL -> {}
                    StateType.LOADING_SONG -> {}
                    StateType.DISMISSING_NORMAL -> {}
                    StateType.DISMISSING_SONG -> {}
                    StateType.EMPTY -> {}
                    StateType.SHOW_TOAST -> {}
                    StateType.LOADING_TOP -> {}
                    StateType.DISMISSING_TOP -> {}
                }
            }
        }
    }

}
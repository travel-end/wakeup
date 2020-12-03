package never.give.up.japp.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment

/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class BaseFragment:Fragment() {
    protected lateinit var mRootView: View
    abstract fun layoutResId():Int
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mRootView = inflater.inflate(layoutResId(),container,false)
        return mRootView
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViewModel()
        initBundle()
        initStatusBar()
        initView()
        initData()
        initAction()
    }
    private fun initBundle() {
        arguments?.let {
            getBundle(it)
        }
    }
    open fun getBundle(bundle: Bundle) {

    }
    open fun initViewModel() {

    }
    open fun initView() {

    }
    open fun initData() {

    }
    open fun initAction() {

    }
    open fun initStatusBar() {

    }

    protected fun nav(id:Int,bundle: Bundle?=null) {
        val nav = NavHostFragment.findNavController(this)
        if (bundle != null) {
            nav.navigate(id,bundle)
        } else {
            nav.navigate(id)
        }
    }
}
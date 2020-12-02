package never.give.up.japp.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class BaseActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId())
        initViewModel()
        initStatusBar()
        initView()
        initData()
        initAction()
    }
    abstract fun layoutResId(): Int
    open fun initView() {
    }
    open fun initData() {
    }
    open fun initAction() {
    }
    open fun initStatusBar() {
    }
    open fun initViewModel() {

    }
}
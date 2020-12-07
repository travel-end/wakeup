package never.give.up.japp.base

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import never.give.up.japp.IMusicService
import never.give.up.japp.play.PlayManager

/**
 * @By Journey 2020/12/2
 * @Description
 */
abstract class BaseActivity:AppCompatActivity(),ServiceConnection {
    private var mToken:PlayManager.ServiceToken?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId())
//        bindService()
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
    private fun bindService() {
        mToken = PlayManager.bindToService(this,this)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        PlayManager.mService = IMusicService.Stub.asInterface(service)
        initData()
        initAction()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        PlayManager.mService = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mToken != null) {
            PlayManager.unbindFromService(mToken)
            mToken = null
        }
    }
}
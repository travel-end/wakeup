package never.give.up.japp.ui.act

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import never.give.up.japp.R
import never.give.up.japp.base.BaseActivity
import never.give.up.japp.consts.Constants
import never.give.up.japp.utils.SpUtil

/**
 * @By Journey 2020/12/7
 * @Description
 */
class WelcomeActivity:BaseActivity() {
    private val mPermission = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val mPermissionCode = 0x122
    override fun layoutResId()= R.layout.activity_welcome
    override fun initView() {
        super.initView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        } else {
            initWelcome()
        }
    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, mPermission,
                mPermissionCode
            )
        } else {
            initWelcome()
        }
    }

    private fun initWelcome() {
        val isFirstLaunch = SpUtil.getBoolean(Constants.KEY_FIRST_LAUNCH,true)
        if (isFirstLaunch) {
            window.decorView.postDelayed({
                startMain()
            }, 3000)
            SpUtil.saveValue(Constants.KEY_FIRST_LAUNCH,false)
        } else {
            window.decorView.postDelayed({
                startMain()
            }, 1000)
        }
    }

    private fun startMain() {
        val mainIntent = Intent(this,MainActivity::class.java)
        mainIntent.action = intent?.action
        startActivity(mainIntent)
        overridePendingTransition(0,0)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == mPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initWelcome()
            } else {
                Toast.makeText(this, "拒绝该权限无法使用该程序", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
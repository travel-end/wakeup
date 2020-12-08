package never.give.up.japp.ui

import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmActivity
import never.give.up.japp.delegate.NavigationManager
import never.give.up.japp.utils.inflate
import never.give.up.japp.vm.MainActViewModel

class MainActivity : BaseVmActivity<MainActViewModel>(),
    NavigationView.OnNavigationItemSelectedListener {
    private var navigationManager: NavigationManager? = null
    override fun layoutResId() = R.layout.activity_main
    override fun initView() {
        super.initView()
        initNavigationView()
    }

    private fun initNavigationView() {
        val navHeader = R.layout.view_nav_header.inflate(navigationView)
        navigationView.addHeaderView(navHeader)
        navigationView.setNavigationItemSelectedListener(this)
        navigationManager = NavigationManager(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawers()
        return navigationManager?.onNavigationItemSelected(item) ?: false
    }
}
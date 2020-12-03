package never.give.up.japp.ui

import kotlinx.android.synthetic.main.frg_main.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.vm.MainFrgViewModel

/**
 * @By Journey 2020/12/2
 * @Description
 */
class MainFragment:BaseVmFragment<MainFrgViewModel>() {
    override fun layoutResId()=R.layout.frg_main
    override fun initAction() {
        super.initAction()
        main_title_search.fastClickListener {
            nav(R.id.action_mainFragment_to_searchMainFragment)
        }
    }
}
package never.give.up.japp.base

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import never.give.up.japp.R

/**
 * @By Journey 2020/10/25
 * @Description
 */
abstract class BaseVpFragment : BaseFragment() {
    protected lateinit var mViewPager: ViewPager2
    protected lateinit var mTabLayout: TabLayout
    private var vpTitles: Array<String>? = null
    private var vpFragments: Array<Fragment>? = null
    private lateinit var mediator: TabLayoutMediator
    override fun initView() {
        super.initView()
        mViewPager = mRootView.findViewById(R.id.viewPager2)
        mTabLayout = mRootView.findViewById(R.id.tabLayout)
    }

    override fun initData() {
        super.initData()
        mViewPager.adapter = VpAdapter()
        mediator = TabLayoutMediator(mTabLayout,mViewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                if (!vpTitles.isNullOrEmpty()) {
                    tab.text = vpTitles!![position]
                }
            })
        mediator.attach()
    }
    open fun initVpTitle(title: Array<String>?) {
        this.vpTitles = title
    }

    open fun initVpFragments(fragments: Array<Fragment>?) {
        this.vpFragments = fragments
    }

    inner class VpAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = if (vpFragments.isNullOrEmpty()) 0 else vpFragments!!.size
        override fun createFragment(position: Int) = vpFragments!![position]
    }

    override fun onDestroy() {
        super.onDestroy()
        mediator.detach()
    }
}
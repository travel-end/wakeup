package never.give.up.japp.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PlayerPagerAdapter(fm: FragmentActivity, private var mFragments: MutableList<Fragment>) : FragmentStateAdapter(fm) {
    fun setFragments(mFragments: MutableList<Fragment>) {
        this.mFragments = mFragments
    }
    override fun getItemCount()=mFragments.size
    override fun createFragment(position: Int)=mFragments[position]
}
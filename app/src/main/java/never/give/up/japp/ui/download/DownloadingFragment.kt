package never.give.up.japp.ui.download

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.frag_normal_musics.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmFragment
import never.give.up.japp.ui.adapter.DownloadingAdapter
import never.give.up.japp.vm.DownloadViewModel

/**
 * @By Journey 2020/12/14
 * @Description
 */
class DownloadingFragment :BaseVmFragment<DownloadViewModel>() {
    private var downloadingAdapter:DownloadingAdapter?=null
    companion object {
        fun newInstance():Fragment {
            val fragment = DownloadingFragment()
            return fragment
        }
    }
    override fun layoutResId()=R.layout.frag_normal_musics

    override fun initView() {
        super.initView()

    }

    override fun initData() {
        super.initData()
        mViewModel.loadDownloadingMusics()
    }

    override fun observe() {
        super.observe()
        mViewModel.downloadingMusics.observe(this,Observer{
            it?.let {
                if (downloadingAdapter==null) {
                    downloadingAdapter = DownloadingAdapter(it)
                    normalRvMusicList.layoutManager = LinearLayoutManager(requireContext())
                    normalRvMusicList.adapter = downloadingAdapter
                } else {
                    downloadingAdapter?.taskList = it
                    downloadingAdapter?.notifyDataSetChanged()
                }
            }
        })
    }

    fun postNotifyDataChanged() {

    }
}
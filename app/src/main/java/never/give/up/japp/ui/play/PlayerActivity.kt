package never.give.up.japp.ui.play

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_player.*
import never.give.up.japp.R
import never.give.up.japp.base.BaseVmActivity
import never.give.up.japp.listener.OnPlayProgressListener
import never.give.up.japp.model.Music
import never.give.up.japp.play.PlayManager
import never.give.up.japp.service.PlayerService
import never.give.up.japp.ui.adapter.PlayerPagerAdapter
import never.give.up.japp.utils.*
import never.give.up.japp.vm.PlayerViewModel

class PlayerActivity:BaseVmActivity<PlayerViewModel>(),OnPlayProgressListener {
    private var playingMusic:Music?=null
    private val coverFragment:PlayCoverFragment = PlayCoverFragment()
    private val lyricFragment:PlayLrcViewFragment = PlayLrcViewFragment()
    private val fragments = mutableListOf<Fragment>()
    override fun layoutResId()=R.layout.activity_player
    override fun initView() {
        super.initView()
        PlayerService.setOnUpdateProgressListener(this)
        PlayerService.getInstance().updatePlayProgress()
        detailView.animation = moveToViewLocation()
        updatePlayMode()
        setupViewPager()
        coverFragment.initAnimator()
        PlayManager.isPlaying().let {
            updatePlayStatus(it)
        }
    }

    override fun initData() {
        super.initData()
        playingMusic = PlayManager.getPlayingMusic()
        if (playingMusic==null) {

        } else {
            titleIv.text= playingMusic?.title
            subTitleTv.text = playingMusic?.artist
            collectIv.isSelected = playingMusic?.isLove == true
            coverFragment.startRotateAnimation(PlayManager.isPlaying())
        }
        CoverLoader.loadBigImageView(this,playingMusic) {bitmap ->
            val result = execute {
                PlayUtil.blurBitmap(bitmap,10)
            }
            coverFragment.setImageBitmap(bitmap)
            playingBgIv.setImageDrawable(result)
        }
    }

    override fun initAction() {
        super.initAction()
    }

    private fun setupViewPager() {
        fragments.clear()
        fragments.add(coverFragment)
        fragments.add(lyricFragment)
        val pagerAdapter = PlayerPagerAdapter(this,fragments)
        playerViewPager.adapter = pagerAdapter
        playerViewPager.offscreenPageLimit=2
        playerViewPager.currentItem=0
        var height = 0
        bottomOpView.post {
            height = bottomOpView.height
        }
        playerViewPager.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (positionOffset <= 1 && position ==0) {
                    detailView.translationY = height* positionOffset
                } else {
                    detailView.translationY = height * 1f
                }
            }

            override fun onPageSelected(position: Int) {
//                if (position == 0) {
//
//                }
            }
        })
    }

    private fun updatePlayStatus(isPlaying:Boolean) {
        if (isPlaying && !playPauseIv.isSelected) {
            playPauseIv.isSelected = true
            coverFragment.resumeRotateAnimation()
        } else if (!isPlaying && playPauseIv.isSelected) {
            coverFragment.stopRotateAnimation()
            playPauseIv.isSelected = false
        }
    }

    private fun updateProgress(progress:Long,max:Long) {
        progressSb.progress = progress.toInt()
        progressSb.max = max.toInt()
        progressTv.text = StringUtil.formatProgress(progress)
        durationTv.text = StringUtil.formatProgress(max)
        lyricFragment.updateLrcTime(progress)
    }

//    private fun setPlayingBitmap(bitmap: Bitmap?) {
//        coverFragment.setImageBitmap(bitmap)
//    }


//    private fun setPlayingBg(d:Drawable?,isInit:Boolean?) {
//        if (isInit != null && isInit) {
//            playingBgIv.setImageDrawable(d)
//        } else {
//            // 加载背景过度图
//            TransitionAnimationUtils.startChangeAnimation(playingBgIv,d)
//        }
//    }

    // 底部上移动画
    private fun moveToViewLocation():TranslateAnimation {
        val hiddenAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_SELF,
            1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        hiddenAction.duration = 300
        return hiddenAction
    }
    private fun updatePlayMode() {
        PlayUtil.updatePlayMode(playModeIv,false)
    }

    override fun onProgressUpdate(position: Int, duration: Int) {
        "播放进度：$position, 播放总时长：$duration".log()
//        updateProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayerService.getInstance().removeUpdateListener()
    }
}
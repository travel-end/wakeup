package never.give.up.japp.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import never.give.up.japp.R
import never.give.up.japp.listener.OnMusicItemClickListener
import never.give.up.japp.model.Music
import never.give.up.japp.play.PlayManager
import never.give.up.japp.utils.*
import never.give.up.japp.widget.RippleView

/**
 * @By Journey 2020/12/14
 * @Description
 */
class NormalMusicAdapter(var musicList:List<Music>):RecyclerView.Adapter<NormalMusicAdapter.NormalMusicViewHolder>() {
    private var onMusicItemClickListener:OnMusicItemClickListener?=null
    fun setOnMusicItemClickListener(listener: OnMusicItemClickListener?) {
        this.onMusicItemClickListener = listener
    }
    class NormalMusicViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var tvSinger:TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_singer)
        var tvSongName:TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_name)
        var rippleView:RippleView = itemView.findViewById(R.id.ripple_view)
        var ivMore:ImageView = itemView.findViewById(R.id.item_search_song_list_iv_more)
        var vPlaying:View = itemView.findViewById(R.id.v_playing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalMusicViewHolder {
        return NormalMusicViewHolder(R.layout.item_normal_music.inflate(parent))
    }

    override fun getItemCount()=musicList.size

    override fun onBindViewHolder(holder: NormalMusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.run {
            tvSinger.text = StringUtil.getArtistAndAlbum(music.artist,music.album)
            tvSongName.text = music.title
            val playingId = PlayManager.getPlayingId()
            if (playingId == music.mid) {
                vPlaying.visible()
                tvSinger.setTextColor(R.color.colorPrimary.getResColor())
                tvSongName.setTextColor(R.color.colorPrimary.getResColor())
            } else {
                if (playingId.isBlank() || playingId=="-1") {

                } else {

                }
            }
            if (music.isCp) {
                tvSinger.setTextColor(R.color.text_color.getResColor())
                tvSongName.setTextColor(R.color.text_color.getResColor())
            }

            rippleView.setOnRippleCompleteListener {
                if (music.isCp) {
                    "歌曲无法播放".toast()
                } else {
                    onMusicItemClickListener?.onMusicItemClick(music,position)
                }
            }
            ivMore.fastClickListener {
                onMusicItemClickListener?.onMoreClick(it)
            }
        }
    }
}
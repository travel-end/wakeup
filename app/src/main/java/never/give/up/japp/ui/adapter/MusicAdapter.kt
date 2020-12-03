package never.give.up.japp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import never.give.up.japp.R
import never.give.up.japp.listener.OnMusicItemClickListener
import never.give.up.japp.model.core.ListBean
import never.give.up.japp.utils.StringUtil
import never.give.up.japp.utils.fastClickListener
import never.give.up.japp.utils.isNotNullOrEmpty
import never.give.up.japp.utils.setDiffColor
import never.give.up.japp.widget.RippleView

/**
 * @By Journey 2020/11/13
 * @Description
 */
class MusicAdapter(private val context: Context, private val searchText: String) :
    RecyclerView.Adapter<MusicAdapter.SearchSingleSongViewHolder>() {
    private var dataList: List<ListBean>? = null
    private var onMusicItemClickListener: OnMusicItemClickListener? = null
    fun setOnMusicItemClickListener(listener: OnMusicItemClickListener) {
        this.onMusicItemClickListener = listener
    }

    fun setData(data: List<ListBean>) {
        this.dataList = data
    }

    class SearchSingleSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSongName: TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_name)
        var tvSinger: TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_singer)
        var tvAlbum: TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_album)
        var tvLyric: TextView = itemView.findViewById(R.id.item_search_song_list_tv_song_lyric)
        var ivIsDownload: ImageView =
            itemView.findViewById(R.id.item_search_song_list_iv_downloaded)
        var ivMoreFunction: ImageView = itemView.findViewById(R.id.item_search_song_list_iv_more)
        var rippleView: RippleView = itemView.findViewById(R.id.ripple_view)
        var playingView: View = itemView.findViewById(R.id.v_playing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSingleSongViewHolder {
        return SearchSingleSongViewHolder(
            (LayoutInflater.from(context).inflate(R.layout.item_music, parent, false))
        )
    }

    override fun getItemCount() = if (isNotNullOrEmpty(dataList)) dataList!!.size else 0

    override fun onBindViewHolder(holder: SearchSingleSongViewHolder, position: Int) {
        if (isNotNullOrEmpty(dataList)) {
            val bean = dataList!![position]
            holder.run {
                tvSongName.text = bean.songname
                tvSinger.text = StringUtil.getSinger(bean)
                tvAlbum.text = bean.albumname
                tvSongName.setDiffColor(searchText, bean.songname)
                tvSinger.setDiffColor(searchText, StringUtil.getSinger(bean))
                tvAlbum.setDiffColor(searchText, bean.albumname)
//                ivIsDownload.isVisible = DownloadedUtil.hasDownloadedSong(bean.songmid?:"")
                if (bean.lyric.isNotNullOrEmpty()) {
                    tvLyric.text = bean.lyric
                    tvLyric.isVisible = true
                }
                rippleView.setOnRippleCompleteListener {
                    onMusicItemClickListener?.onMusicItemClick(bean, position)
                }
                ivMoreFunction.fastClickListener {
                    onMusicItemClickListener?.onMoreClick(it)
                }
            }
        }
    }
}
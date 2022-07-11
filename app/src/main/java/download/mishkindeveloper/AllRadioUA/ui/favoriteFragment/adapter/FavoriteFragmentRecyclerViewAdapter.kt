package download.mishkindeveloper.AllRadioUA.ui.favoriteFragment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.enums.DisplayListType
import download.mishkindeveloper.AllRadioUA.services.PlayerService
import com.squareup.picasso.Picasso

class FavoriteFragmentRecyclerViewAdapter(
    private var items: List<RadioWave>,
    var context: Context?,
    var mPlayer: ExoPlayer,
    var mService: PlayerService
) :
    RecyclerView.Adapter<FavoriteViewHolder>() {
    private val grid = 0
    private val list = 1
    private var displayListType: DisplayListType? = null
    private val waveViewHolder:FavoriteViewHolder? = null

    fun setDisplayListType(displayListType: DisplayListType) {
        this.displayListType = displayListType
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayListType == DisplayListType.List) {
            return list
        } else {
            return grid
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        when (viewType) {
            list -> {
                return FavoriteViewHolder(
                    layoutInflater.inflate(
                        R.layout.wave_items_list,
                        parent,
                        false
                    )
                )
            }
            grid -> {
                return FavoriteViewHolder(
                    layoutInflater.inflate(
                        R.layout.wave_items_grid,
                        parent,
                        false
                    )
                )
            }
        }
        return waveViewHolder!!   }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.frequencyTextView?.text = items[position].fmFrequency
        holder.nameTextView?.text = items[position].name
        Picasso.get()
            .load(items[position].image)
            .into(holder.imageViewWave)
        holder.itemView.setOnClickListener {
            setRadioWaveAndPlay(position)
        }
    }

    private fun setRadioWaveAndPlay(position: Int) {
        val mediaItem: MediaItem = MediaItem.fromUri(items[position].url.toString())
        mPlayer.setMediaItem(mediaItem)
        mPlayer.play()
        mService.setRadioWave(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
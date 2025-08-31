import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.enums.DisplayListType
import download.mishkindeveloper.AllRadioUA.listeners.MenuItemIdListener
import download.mishkindeveloper.AllRadioUA.services.PlayerService
import download.mishkindeveloper.AllRadioUA.ui.listFragment.adapter.WaveViewHolder

class ListFragmentRecyclerViewAdapter(
    var context: Context?,
    var mPlayer: ExoPlayer,
    var mService: PlayerService,
    private var menuItemIdListener: MenuItemIdListener,
    diffCallback: RadioWaveDiffCallback
) :
    ListAdapter<RadioWave, WaveViewHolder>(diffCallback) {
    private val grid = 0
    private val list = 1
    private var displayListType: DisplayListType? = null

    override fun getItemViewType(position: Int): Int {
        return if (displayListType == DisplayListType.List) {
            list
        } else {
            grid
        }
    }

    fun setDisplayListType(displayListType: DisplayListType) {
        this.displayListType = displayListType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaveViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return when (viewType) {
            list -> WaveViewHolder(layoutInflater.inflate(R.layout.wave_items_list, parent, false))
            grid -> WaveViewHolder(layoutInflater.inflate(R.layout.wave_items_grid, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: WaveViewHolder, position: Int) {
        val radioWave = getItem(position)
        holder.frequencyTextView?.text = radioWave.fmFrequency
        holder.nameTextView?.text = radioWave.name

        checkImageNull(radioWave, holder)
        checkFavItem(radioWave, holder)
        checkCustomItem(radioWave, holder)

        holder.menuImageButton?.setOnClickListener {
            menuItemIdListener.getItemMenu(radioWave.id)
        }

        holder.itemView.setOnClickListener {
            setMediaItem(radioWave)
            menuItemIdListener.updateCountOpenItem(radioWave.id)
        }

        radioWaveNameEquals(radioWave, holder)
    }

    private fun checkCustomItem(radioWave: RadioWave, holder: WaveViewHolder) {
        holder.menuImageButton?.isVisible = radioWave.custom == true
    }

    private fun checkFavItem(radioWave: RadioWave, holder: WaveViewHolder) {
        holder.favImageView?.isVisible = radioWave.favorite == true
    }

    private fun checkImageNull(radioWave: RadioWave, holder: WaveViewHolder) {
        if (TextUtils.isEmpty(radioWave.image)) {
            holder.imageViewWave?.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Picasso.get()
                .load(radioWave.image)
                .into(holder.imageViewWave)
        }
    }

    private fun radioWaveNameEquals(radioWave: RadioWave, holder: WaveViewHolder) {
        holder.lottieAnimationView?.isVisible = mService.getRadioWave()?.name == radioWave.name
    }

    private fun setMediaItem(radioWave: RadioWave) {
        val mediaItem = MediaItem.fromUri(radioWave.url.toString())
        mService.getPlayer()?.setMediaItem(mediaItem)
        mService.getPlayer()?.prepare()
        mService.getPlayer()?.play()
        mService.setRadioWave(radioWave)
        notifyDataSetChanged()
    }

    class RadioWaveDiffCallback : DiffUtil.ItemCallback<RadioWave>() {
        override fun areItemsTheSame(oldItem: RadioWave, newItem: RadioWave): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RadioWave, newItem: RadioWave): Boolean {
            return oldItem == newItem
        }
    }
}

package download.mishkindeveloper.AllRadioUA.ui.favoriteFragment.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import download.mishkindeveloper.AllRadioUA.R

class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var imageViewWave: ImageView? = itemView.findViewById(R.id.imageViewWave)
    var nameTextView: TextView? = itemView.findViewById(R.id.textViewTrackName)
    var frequencyTextView: TextView? = itemView.findViewById(R.id.historyDateTextView)
    var lottieAnimationView: LottieAnimationView? =itemView.findViewById(R.id.favAnimationView)
}
package download.mishkindeveloper.AllRadioUA.ui.main

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airbnb.lottie.LottieAnimationView
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
import com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.*
import com.google.firebase.database.annotations.NotNull
import com.squareup.picasso.Picasso
import dagger.android.AndroidInjection
import de.hdodenhof.circleimageview.CircleImageView
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.data.entity.Track
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import download.mishkindeveloper.AllRadioUA.listeners.FragmentSettingListener
import download.mishkindeveloper.AllRadioUA.services.PlayerService
import download.mishkindeveloper.AllRadioUA.services.TimerService
import download.mishkindeveloper.AllRadioUA.ui.favoriteFragment.FavoriteFragment
import download.mishkindeveloper.AllRadioUA.ui.historyFragment.HistoryFragment
import download.mishkindeveloper.AllRadioUA.ui.listFragment.ListFragment
import download.mishkindeveloper.AllRadioUA.ui.settingFragment.SettingFragment
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private var mExoPlayer: ExoPlayer? = null
    private var mPlayerService: PlayerService? = null
    private  var mPlayerView: PlayerControlView? = null
    private  var database: DatabaseReference? = null
    private  var bottomNavView: BottomNavigationView? = null
    private var fragmentView: FragmentContainerView? = null
    private var items: MutableList<RadioWave> = mutableListOf<RadioWave>()
    private  var mPosterImageView: CircleImageView? = null
    private  var mNameTextView: TextView? = null
    private  var mFmFrequencyTextView: TextView? = null
    private  var radioWave: RadioWave? = null
    private  var lottieAnimationView: LottieAnimationView? = null
    private  var mVisualizer: CircleLineVisualizer? = null
    private var audioSessionId by Delegates.notNull<Int>()
    private  var motionLayout: MotionLayout? = null
    private  var favoriteImageButton: ImageButton? = null
    private var playImageView: ImageView? = null
    private var animNetLottieAnimationView: LottieAnimationView? = null
    private var backImageButton: ImageButton? = null
    private var titleToolTextView: TextView? = null
    private var searchView: SearchView? = null
    private var timerTextView: TextView? = null
    private var timerImageButton: ImageButton? = null
    private var addImageButton: ImageButton? = null
    private var titleTextViewPlayer: TextView? = null
    private var trackInfoMiniPlayerTextView: TextView? = null
    private var artistPoster = ""
    private var fragmentSettingListener: FragmentSettingListener? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var preferencesHelper: PreferenceHelper
    private lateinit var titleTextView: TextView
    private lateinit var posterImageView: ImageView
    private lateinit var fragment: Fragment
    private var firstStartStatus: Boolean = true
    private lateinit var searchImageButton: ImageButton
    private lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null

    private val radioWaveBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val radioWave: RadioWave =
                intent.getSerializableExtra(getString(R.string.serializable_extra)) as RadioWave
            titleTextView.text = radioWave.name
            Picasso.get()
                .load(radioWave.image)
                .into(posterImageView)
            preferencesHelper.setIdPlayMedia(radioWave.id!!)
        }
    }

    private var interstitialAdLoadCallback: InterstitialAdLoadCallback =
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                mInterstitialAd = p0
            }

        }

    private fun initAds() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            interstitialAdLoadCallback
        )
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        initPermission()
        checkFirstStartStatus()
        initBroadcastManager()
        setMediaInfoInMiniPlayer()
        setListeners()
        performSearch()
        initAds()

    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(radioWaveBroadcastReceiver)
        setMediaInfoInMiniPlayer()
        mAdView.destroy()
    }

    private val timerBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateGUI(intent!!)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(timerBroadcastReceiver, IntentFilter(getString(R.string.intent_filter)))
        mAdView.resume()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(timerBroadcastReceiver)
    }

    override fun onStop() {
        try {
            unregisterReceiver(timerBroadcastReceiver)
        } catch (e: java.lang.Exception) {
            e.stackTrace
        }
        super.onStop()
    }

    private fun checkFirstStartStatus() {
        firstStartStatus = preferencesHelper.getFirstStart()
        if (firstStartStatus) {
            initDb()
        } else {
            startPlayerService()
        }
    }

    private fun initBroadcastManager() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                radioWaveBroadcastReceiver,
                IntentFilter(getString(R.string.intent_filter_notification))
            )
    }

    private fun favoriteStatusFalse() {
        radioWave?.favorite = true
        viewModel.updateRadioWave(radioWave)
        favoriteImageButton?.setImageResource(R.drawable.ic_baseline_favorite_24)
        lottieAnimationView?.visibility = View.VISIBLE
        lottieAnimationView?.playAnimation()

    }

    private fun favoriteStatusTrue() {
        radioWave?.favorite = false
        viewModel.updateRadioWave(radioWave)
        favoriteImageButton?.setImageResource(R.drawable.ic_baseline_favorite_border_24)

    }

    private fun initRadioWaveFromService() {
        radioWave = mPlayerService?.getRadioWave()!!
        if (radioWave?.favorite == false) {
            favoriteStatusFalse()
        } else {
            favoriteStatusTrue()
        }
    }

    private var lottieAnimationListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            lottieAnimationView?.visibility = View.INVISIBLE
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        favoriteImageButton?.setOnClickListener {
            initRadioWaveFromService()
        }
        timerImageButton?.setOnClickListener {
            timerTextView?.visibility = View.VISIBLE
            createTimerAlertDialog()
        }
        addImageButton?.setOnClickListener { createInsertAlertDialog() }
        backImageButton?.setOnClickListener { motionLayout?.transitionToStart() }

        lottieAnimationView?.addAnimatorListener(lottieAnimationListener)
        bottomNavView?.setOnItemSelectedListener(bottomNavViewOnItemSelectListener)
        playImageView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                checkStatusClickPlayInMiniPlayer()
            }
            false
        }
        searchImageButton.setOnClickListener {
            checkStatusSearchViewVisible()
        }
    }

    private fun checkStatusSearchViewVisible() {
        if (searchView?.visibility == View.VISIBLE) {
            searchView?.visibility = View.GONE
            titleToolTextView?.visibility = View.VISIBLE
        } else {
            searchView?.visibility = View.VISIBLE
            titleToolTextView?.visibility = View.GONE
        }
    }

    private fun checkStatusClickPlayInMiniPlayer() {
        if (mExoPlayer!!.isPlaying) {
            mExoPlayer!!.pause()
        } else {
            mExoPlayer!!.play()
        }
    }

    private fun setMediaInfoInMiniPlayer() {
        val id: Int = preferencesHelper.getIdPlayMedia()
        try {
            setTitleMiniPlayer(id)
        } catch (e: Exception) {
            e.stackTrace
            setTitleMiniPlayer(1)
        }

    }

    private fun setTitleMiniPlayer(id: Int) {
        val radioWave: RadioWave = viewModel.getRadioWaveForId(id)
        titleTextView.text = radioWave.name
        Picasso.get()
            .load(radioWave.image)
            .into(posterImageView)
        preferencesHelper.setIdPlayMedia(radioWave.id)
    }

    private fun createListFragment() {
        fragment = ListFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.list_menu_item)

    }

    private fun createSettingFragment() {
        fragment = SettingFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.set_menu_item)
    }

    private fun createHistoryFragment() {
        fragment = HistoryFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.history_menu_item)
    }

    private fun createFavFragment() {
        fragment = FavoriteFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.fav_menu_item)
    }

    private var bottomNavViewOnItemSelectListener = NavigationBarView.OnItemSelectedListener {
        when (it.itemId) {
            R.id.listFragmentItem -> {
                createListFragment()
                loadPageAds()
                searchImageButton.visibility = View.VISIBLE
            }
            R.id.favoriteFragmentItem -> {
                createFavFragment()
                loadPageAds()
                searchImageButton.visibility = View.INVISIBLE
            }
            R.id.settingFragmentItem -> {
                createSettingFragment()
                loadPageAds()
                searchImageButton.visibility = View.INVISIBLE
            }
            R.id.historyFragmentItem -> {
                createHistoryFragment()
                loadPageAds()
                searchImageButton.visibility = View.INVISIBLE
            }
        }
        return@OnItemSelectedListener true
    }

    private fun loadPageAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
        }
    }

    private fun loadBanner(progress: Float) {
        if (progress > 0.99F) {
            mAdView.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
        } else {
            mAdView.visibility = View.GONE
        }
    }

    private val transitionListener = object : MotionLayout.TransitionListener {
        override fun onTransitionStarted(p0: MotionLayout?, startId: Int, endId: Int) {}

        override fun onTransitionChange(
            p0: MotionLayout?,
            startId: Int,
            endId: Int,
            progress: Float
        ) {
            loadBanner(progress)
        }

        override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
            setParamMediaIfScrollMiniPlayer()
            checkButtonPlayInMiniPlayer()
            if (mPlayerService == null) return
            setMediaSessionAndVisual()
        }

        override fun onTransitionTrigger(
            p0: MotionLayout?,
            triggerId: Int,
            positive: Boolean,
            progress: Float
        ) {
        }
    }

    private fun checkButtonPlayInMiniPlayer() {
        if (mPlayerService?.getRadioWave()?.favorite == true) {
            favoriteImageButton?.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            favoriteImageButton?.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
    }

    private fun setParamMediaIfScrollMiniPlayer() {
        val id: Int = preferencesHelper.getIdPlayMedia()
        radioWave = viewModel.getRadioWaveForId(id)
        Picasso.get()
            .load(mPlayerService?.getRadioWave()?.image)
            .resize(150, 150)
            .into(mPosterImageView)
        mPlayerView?.player = mPlayerService?.getPlayer()
        mNameTextView?.text = mPlayerService?.getRadioWave()?.name
        mFmFrequencyTextView?.text = mPlayerService?.getRadioWave()?.fmFrequency
    }

    private fun setMediaSessionAndVisual() {
        audioSessionId = mExoPlayer!!.audioSessionId
        try {
            mVisualizer?.setAudioSessionId(audioSessionId)
        } catch (e: Exception) {
            mVisualizer?.release()
            mVisualizer?.setAudioSessionId(audioSessionId)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        mAdView = findViewById(R.id.adView)
        mPlayerView = findViewById(R.id.playerView)
        bottomNavView = findViewById(R.id.bottomNavViewMain)
        fragmentView = findViewById(R.id.fragmentContainerView)
        mPosterImageView = findViewById(R.id.imageViewPoster)
        mNameTextView = findViewById(R.id.nameTextView)
        mFmFrequencyTextView = findViewById(R.id.fmFrequencyTextView)
        mVisualizer = findViewById(R.id.blob)
        lottieAnimationView = findViewById(R.id.favAnimationView)
        favoriteImageButton = findViewById(R.id.favoriteImageButton)
        motionLayout = findViewById(R.id.motion_layout)
        titleTextView = findViewById(R.id.title_textView)
        motionLayout?.addTransitionListener(transitionListener)
        posterImageView = findViewById(R.id.main_imageView)
        playImageView = findViewById(R.id.play_imageView)
        animNetLottieAnimationView = findViewById(R.id.netAnim)
        backImageButton = findViewById(R.id.backImageButton)
        titleToolTextView = findViewById(R.id.titleToolTextView)
        titleToolTextView?.text = getString(R.string.list_menu_item)
        searchView = findViewById(R.id.radio_search)
        timerTextView = findViewById(R.id.timerTextView)
        timerImageButton = findViewById(R.id.timerImageButton)
        addImageButton = findViewById(R.id.addImageButton)
        searchImageButton = findViewById(R.id.searchImageButton)
        titleTextViewPlayer = findViewById(R.id.titlePlayerTextView)
        trackInfoMiniPlayerTextView = findViewById(R.id.track_info_textView)
        trackInfoMiniPlayerTextView?.isSelected = true

    }


    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    fun initDb() {
        database =
            FirebaseDatabase.getInstance(getString(R.string.firebase_url))
                .getReference(getString(R.string.firebase_ref))

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull @NotNull snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val radioWave: RadioWave? = dataSnapshot.getValue(RadioWave::class.java)
                    items.add(radioWave!!)
                }
                viewModel.createListRadioWave(items)
                startPlayerService()
                createListFragment()
                preferencesHelper.setFirstStart(false)
                preferencesHelper.setIdPlayMedia(items[2].id)
            }

            override fun onCancelled(@NonNull @NotNull error: DatabaseError) {}
        }
        database?.addValueEventListener(valueEventListener)
    }

    fun updateDb() {
        database =
            FirebaseDatabase.getInstance(getString(R.string.firebase_url))
                .getReference(getString(R.string.firebase_ref))
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull @NotNull snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val radioWave: RadioWave? = dataSnapshot.getValue(RadioWave::class.java)
                    items.add(radioWave!!)
                }
                viewModel.createListRadioWave(items)
                preferencesHelper.setFirstStart(false)
            }

            override fun onCancelled(@NonNull @NotNull error: DatabaseError) {}
        }
        database?.addValueEventListener(valueEventListener)
    }

    private fun setMediaItem() {
        val id = preferencesHelper.getIdPlayMedia()
        val url: String?
        try {
            if (mExoPlayer!!.currentMediaItem == null) {
                url = viewModel.getRadioWaveForId(id).url
                val mediaItem: MediaItem =
                    MediaItem.fromUri(url!!)
                mPlayerService?.getPlayer()?.setMediaItem(mediaItem)
                mPlayerService?.setRadioWave(viewModel.getRadioWaveForId(id))



            }
        } catch (e: NullPointerException) {
            e.stackTrace
        }
    }

    private var myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            mPlayerService = (binder as PlayerService.PlayerBinder).getService()
            mExoPlayer = mPlayerService?.getPlayer()
            mPlayerService?.getRadioWave()?.id?.let { preferencesHelper.setIdPlayMedia(it) }
            setMediaItem()
            isPlayingMedia(mExoPlayer!!.isPlaying)
            setTrackInfo()
            mPlayerService?.getPlayer()?.addListener(playerListener)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mPlayerService = null
            mExoPlayer = null
        }
    }

    private fun setTrackInfo() {
        if (mPlayerService?.getPlayer()?.mediaMetadata?.title != null) {
            trackInfoMiniPlayerTextView?.text =
                mPlayerService?.getPlayer()?.mediaMetadata?.title.toString()
            titleTextViewPlayer?.text =
                mPlayerService?.getPlayer()?.mediaMetadata?.title.toString()
        }
    }

    private var playerListener = object : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            if (mediaMetadata.title != null) {

//               var view = findViewById<View>(R.id.favAnimationView)
//                view.isVisible = true

                titleTextViewPlayer?.text = mediaMetadata.title.toString()
                trackInfoMiniPlayerTextView?.text = mediaMetadata.title.toString()
                mediaMetadataCheckEmptyAndContains(mediaMetadata)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            isPlayingMedia(isPlaying)
        }

        private fun mediaMetadataCheckEmptyAndContains(mediaMetadata: MediaMetadata) {
            if (!mediaMetadata.title.isNullOrEmpty()) {
                if (mediaMetadata.title.toString().contains("-") and
                    !mediaMetadata.title.toString()
                        .contains(mediaMetadata.station.toString()) and
                    !mediaMetadata.title.toString().contains("UNKNOWN") and
                    !mediaMetadata.title.toString().contains("RADIO") and
                    !mediaMetadata.title.toString().contains("=›") and
                    !mediaMetadata.title.toString().contains(".UA") and
                    !mediaMetadata.title.toString().contains("www")
                ) {
                    posterRequestOkhttp(mediaMetadata)
                }
            }
        }


        private fun posterRequestOkhttp(mediaMetadata: MediaMetadata) {
            val artist = mediaMetadata.title.toString().split("-")
            val url =
                "https://www.theaudiodb.com/api/v1/json/2/search.php?s=${artist[0]}"
            val okHttpClient: OkHttpClient = OkHttpClient()
            val request: Request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                }

                @SuppressLint("SimpleDateFormat")
                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()?.let { JSONObject(it) }
                    val jsonArray: JSONArray
                    try {
                        jsonArray = json!!.getJSONArray("artists")
                        runOnUiThread {
                            insertTrackAndLoadPoster(mediaMetadata, jsonArray)
                        }
                    } catch (e: java.lang.Exception) {
                        runOnUiThread {
                            insertTrackAndSetDefaultPoster(mediaMetadata)
                        }
                    }
                }
            })
        }

        @SuppressLint("SimpleDateFormat")
        private fun insertTrackAndSetDefaultPoster(mediaMetadata: MediaMetadata) {
            artistPoster =
                "https://i.ibb.co/G3yqPVB/generalimage.jpg"
            val track = Track()
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
            val currentDate = sdf.format(Date())
            track.name = mediaMetadata.title.toString()
            track.date = currentDate
            track.image = artistPoster.toString()
            track.station = mediaMetadata.station.toString()
            viewModel.insertTrack(track)
        }

        @SuppressLint("SimpleDateFormat")
        private fun insertTrackAndLoadPoster(
            mediaMetadata: MediaMetadata,
            jsonArray: JSONArray
        ) {
            val track = Track()
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
            val currentDate = sdf.format(Date())
            track.name = mediaMetadata.title.toString()
            artistPoster =
                jsonArray.getJSONObject(0)?.getString("strArtistFanart").toString()
            track.date = currentDate
            track.image = artistPoster.toString()
            track.station = mediaMetadata.station.toString()
            viewModel.insertTrack(track)
        }

        override fun onPlayerError(error: PlaybackException) {
            when (error.errorCode) {
                ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                    animNetLottieAnimationView?.visibility = View.VISIBLE
                }
                ERROR_CODE_IO_FILE_NOT_FOUND -> {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.error_payback),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            animNetLottieAnimationView?.visibility = View.INVISIBLE
        }
    }

    private fun performSearch() {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fragmentSettingListener?.search(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                fragmentSettingListener?.search(newText)
                return true
            }
        })
    }


    private fun isPlayingMedia(isPlaying: Boolean) {
        if (isPlaying) {
            playImageView?.setImageResource(R.drawable.ic_baseline_pause_24)
        } else {
            playImageView?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    private fun startPlayerService() {
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, myConnection, BIND_AUTO_CREATE)
        startService(intent)
    }

    override fun onBackPressed() {
        motionLayout?.transitionToStart()
    }

    @SuppressLint("SetTextI18n")
    private fun updateGUI(intent: Intent) {
        if (intent.extras != null) {
            timerImageButton?.setImageResource(R.drawable.ic_baseline_timer_red_24)
            timerImageButton?.tag = getString(R.string.tag_work)
            val millisUntilFinished =
                intent.getLongExtra(getString(R.string.serializable_extra_long), 0)
            val min: Long = (millisUntilFinished / 1000) / 60
            val sec: Long = (millisUntilFinished / 1000) % 60
            timerTextView?.text = "$min:$sec " + getString(R.string.minute_title)
            if (sec == 0L) {
                mExoPlayer?.stop()
                stopService(Intent(this, PlayerService::class.java))
                timerTextView?.visibility = View.GONE
                timerImageButton?.setImageResource(R.drawable.ic_baseline_timer_24)
                timerImageButton?.tag = getString(R.string.tag_stop)
            }
        }
    }

    private fun ifTagWork(
        stopTimerButton: Button,
        timerTextViewDialog: TextView,
        minTextView: TextView,
        setTimerButton: Button, minuteEditText: EditText
    ) {
        setTimerButton.visibility = View.GONE
        stopTimerButton.visibility = View.VISIBLE
        minuteEditText.visibility = View.GONE
        timerTextViewDialog.text = getString(R.string.timer_work)
        minTextView.visibility = View.GONE
    }

    private fun stopTimerEvents(
        stopTimerButton: Button,
        timerTextViewDialog: TextView,
        minTextView: TextView,
        setTimerButton: Button,
        minuteEditText: EditText
    ) {
        stopService(Intent(this, TimerService::class.java))
        setTimerButton.visibility = View.VISIBLE
        stopTimerButton.visibility = View.GONE
        minuteEditText.visibility = View.VISIBLE
        timerTextView?.visibility = View.GONE
        timerImageButton?.setImageResource(R.drawable.ic_baseline_timer_24)
        timerTextViewDialog.text = getString(R.string.timer_set)
        timerImageButton?.tag = getString(R.string.tag_stop)
        minTextView.visibility = View.VISIBLE
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createTimerAlertDialog() {
        val builder = AlertDialog.Builder(this)
            .create()
        val view = layoutInflater.inflate(R.layout.timer_custom_alert_dialog, null)
        val setTimerButton = view.findViewById<Button>(R.id.setTimerButton)
        val minuteEditTextDialog = view.findViewById<EditText>(R.id.minuteEditText)
        val stopTimerButton = view.findViewById<Button>(R.id.stopTimerButton)
        val timerTextViewDialog = view.findViewById<TextView>(R.id.timerTextViewDialog)
        val minTextView = view.findViewById<TextView>(R.id.minTextView)
        if (timerImageButton?.tag == getString(R.string.tag_work)) {
            ifTagWork(
                stopTimerButton,
                timerTextViewDialog,
                minTextView,
                setTimerButton,
                minuteEditTextDialog
            )
        }
        stopTimerButton.setOnClickListener {
            stopTimerEvents(
                stopTimerButton,
                timerTextViewDialog,
                minTextView,
                setTimerButton,
                minuteEditTextDialog
            )
        }

        builder.setView(view)
        setTimerButton.setOnClickListener {
            startTimerService(minuteEditTextDialog)
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(true)
        builder.show()
    }

    private fun startTimerService(minuteEditTextDialog: EditText) {
        val intent = Intent(this, TimerService::class.java)
        intent.putExtra(
            getString(R.string.serializable_extra_min),
            minuteEditTextDialog.text.toString()
        )
        startService(intent)
    }

    private fun createInsertAlertDialog() {
        val builder = AlertDialog.Builder(this)
            .create()
        val view = layoutInflater.inflate(R.layout.add_update_radio_wave_alert_dialog, null)
        val saveButton = view.findViewById<ImageButton>(R.id.saveButton)
        val nameEditText = view.findViewById<EditText>(R.id.name_edit_text)
        val urlEditText = view.findViewById<EditText>(R.id.url_edit_text)
        saveButton.setOnClickListener {
            insertRadioWave(nameEditText, urlEditText, builder)
        }
        builder.setView(view)
        builder.setCanceledOnTouchOutside(true)
        builder.show()
    }

    private fun insertRadioWave(
        nameEditText: EditText,
        urlEditText: EditText,
        builder: AlertDialog
    ) {
        val radioWave = RadioWave()
        radioWave.name = nameEditText.text.toString()
        radioWave.image = getString(R.string.default_logo_url)
        radioWave.custom = true
        radioWave.url = urlEditText.text.toString()
        if (nameEditText.text.trim { it <= ' ' }
                .isEmpty() || urlEditText.text.trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(this, getText(R.string.empty_edit_text), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertRadioWave(radioWave)
            fragmentSettingListener?.update()
            builder.dismiss()
        }
    }

    fun setSettingListener(fragmentSettingListener: FragmentSettingListener) {
        this.fragmentSettingListener = fragmentSettingListener
    }}

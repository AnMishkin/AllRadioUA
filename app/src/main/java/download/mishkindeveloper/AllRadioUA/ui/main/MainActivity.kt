package download.mishkindeveloper.AllRadioUA.ui.main


import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
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
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlaybackException.*
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.database.*
import com.google.firebase.database.annotations.NotNull
import com.squareup.picasso.Picasso
import dagger.android.AndroidInjection
import de.hdodenhof.circleimageview.CircleImageView
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.ReviewManager.ReviewManager
import download.mishkindeveloper.AllRadioUA.alarm.*

import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.data.entity.Track
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import download.mishkindeveloper.AllRadioUA.listeners.FragmentSettingListener
import download.mishkindeveloper.AllRadioUA.listeners.MenuItemIdListener
import download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService
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
import android.content.pm.ServiceInfo

class MainActivity : AppCompatActivity() {
    private val RECORD_PERMISSION_CODE = 1

    private lateinit var timer: Timer
    private val noDelay = 5000L
    private val everyFiveSeconds = 5000L

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
    //заменил
    private var audioSessionId: Int? = null
    var motionLayout: MotionLayout? = null
    private  var favoriteImageButton: ImageButton? = null
    private var playImageView: ImageView? = null
    private var animNetLottieAnimationView: LottieAnimationView? = null
    private var backImageButton: ImageButton? = null
    private var titleToolTextView: TextView? = null
    private var searchView: SearchView? = null
    private var timerTextView: TextView? = null
    private var timerImageButton: ImageButton? = null
    private var alertImageButton: ImageButton? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var calendar: Calendar
    private lateinit var pendingIntent: PendingIntent
    private lateinit var timePicker: MaterialTimePicker
    private lateinit var alarmRecyclerView: RecyclerView
    private lateinit var alarmAdapter: RadioStationAdapter
    private var selectedRadioStation: RadioWave? = null
    private var menuItemIdListener: MenuItemIdListener? = null

    private var addImageButton: ImageButton? = null
    private var titleTextViewPlayer: TextView? = null
    private var trackInfoMiniPlayerTextView: TextView? = null
    private var artistPoster = ""
    private var fragmentSettingListener: FragmentSettingListener? = null
    private lateinit var preferenceAlarmHelper: PreferenceAlarmHelper
    private var mPlayerAlarmService: AlarmRadioPlayerService? = null
    private var alarmPlayerService: AlarmRadioPlayerService? = null
    private var isServiceBound = false
    val packageNameForAlarm = "download.mishkindeveloper.AllRadioUA.services"
    val serviceClassName = "download.mishkindeveloper.AllRadioUA.services.AlarmRadioPlayerService"
    private var stopAlarmActivity: StopAlarmActivity? = null
    private var alarmPendingIntent: PendingIntent? = null


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

    lateinit var mAppUpdateManager: AppUpdateManager
    private val RC_APP_UPDATE = 100
    private var updateCanceled: String? = null
    private var newAppIsReady: String? = null
    private var updateInstall: String? = null

    private lateinit var textReview : String
    private lateinit var laiterReview : String
    private lateinit var leaveReview : String
    private lateinit var okReview : String
    private var alarmRadioPlayerService: AlarmRadioPlayerService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlarmRadioPlayerService.PlayerBinder
            alarmRadioPlayerService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            alarmRadioPlayerService = null
        }
    }


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
            "ca-app-pub-3971991853344828/7136755739",
            adRequest,
            interstitialAdLoadCallback
        )
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            }

            override fun onAdShowedFullScreenContent() {
//                mInterstitialAd = null
//                initAds()
//                Toast.makeText(this@MainActivity, "загрузилась реклама", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        checkAlarm()
        initPermission()
        checkFirstStartStatus()
        initBroadcastManager()
        //createNotificationChannelAlarm()
        setMediaInfoInMiniPlayer()
        setListeners()
        performSearch()
        initAds()

        ReviewManager(this).checkAndPromptForReview(textReview, laiterReview, leaveReview,okReview)

        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        mAppUpdateManager.registerListener(installStateUpdatedListener)

        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.FLEXIBLE, this, RC_APP_UPDATE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }


// Implement InstallStateUpdatedListener interface
        val installStateUpdatedListener = InstallStateUpdatedListener {
            // Handle update state changes here
        }



        //isRecordAudioPermissionGranted()

        //checkDate()
        //titleToolTextView?.text = items.size.toString()+"-"+getString(R.string.list_menu_item)
    }

    private fun checkAlarm() {
        var check = preferenceAlarmHelper.getBoolean("Alarm",false)
        if (check) {
            alertImageButton?.setImageResource(R.drawable.baseline_set_alert_24)
        } else {
            alertImageButton?.setImageResource(R.drawable.baseline_add_alert_24)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(radioWaveBroadcastReceiver)
        setMediaInfoInMiniPlayer()
        mAdView.destroy()
        mExoPlayer?.stop()
        mVisualizer?.release()

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
        //requestRecordPermission()
        // Проверка состояния обновления
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // Обновление было загружено, отображаем сообщение
                showCompletedUpdate()
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Процесс обновления был приостановлен, возобновляем его
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        RC_APP_UPDATE
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(timerBroadcastReceiver)
    }

    override fun onStop() {
        try {
            motionLayout?.transitionToEnd()
            unregisterReceiver(timerBroadcastReceiver)

        } catch (e: java.lang.Exception) {
            e.stackTrace
        }
        mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop()
    }

    private fun checkFirstStartStatus() {
        firstStartStatus = preferencesHelper.getFirstStart()
        if (firstStartStatus) {
            initDb()
            updateDb()

        } else {
            updateDb()
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
        Log.d("Mylog","Клацнули кнопку добавить в ибранное")
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
    fun setListeners() {
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
        searchImageButton?.setOnClickListener {
            checkStatusSearchViewVisible()
        }

        //кнопка будильника
        alertImageButton?.setOnClickListener {
            var prefAlarm = preferenceAlarmHelper.getBoolean("Alarm",true)
            Log.d("MyLog","$prefAlarm")
            if (prefAlarm) {
                //val stopAlarmIntent = Intent(this, StopAlarmActivity::class.java)
                //stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                //startActivity(stopAlarmIntent)
                //StopAlarmActivity().stopAlarmDialog()

                stopAlarmDialogInMain()
            } else {
                alertImageButton?.setImageResource(R.drawable.baseline_set_alert_24)
                createAlarmDialog()

            }
        }



    }

    fun onRadioStationSelected(radioStation: RadioWave) {
        // Здесь вы получаете выбранную радиостанцию и можете сохранить ее для использования в будильнике
        val selectedRadioStationName = radioStation.name

        // Продолжайте реализацию, например, сохраните выбранную радиостанцию в SharedPreferences
    }


    private fun createAlarmDialog() {
        var timePickerTitle = getString(R.string.time_picker_text)
        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText(timePickerTitle)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("AlarmHour", selectedHour)
            editor.putInt("AlarmMinute", selectedMinute)
            editor.apply()
            alertImageButton?.setImageResource(R.drawable.baseline_set_alert_24)
            createAlarmFragment()
        }

        timePicker.addOnNegativeButtonClickListener {
            alertImageButton?.setImageResource(R.drawable.baseline_add_alert_24)
        }

        timePicker.show(supportFragmentManager, "timePicker")
    }

    fun createAlarmFragment() {
        fragment = AlarmFragment().newInstance()
        val adapter = RadioStationAdapter(items, this, mPlayerService, menuItemIdListener)
        val sharedPreferences = getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        val hour = sharedPreferences.getInt("AlarmHour", 0)
        val minute = sharedPreferences.getInt("AlarmMinute", 0)
        adapter.setAlarmTime(hour, minute)
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        initAds()
        Log.d("Mylog","создается список станций для будильника")
    }


    fun checkStatusSearchViewVisible() {
        if (searchView?.visibility == View.VISIBLE) {
            searchView?.visibility = View.GONE
            titleToolTextView?.visibility = View.VISIBLE
        } else {
            searchView?.visibility = View.VISIBLE
            titleToolTextView?.visibility = View.GONE
        }
    }

    fun checkStatusClickPlayInMiniPlayer() {
        if (mExoPlayer != null) {
            if (mExoPlayer!!.isPlaying) {
                mExoPlayer!!.pause()
            } else {
                mExoPlayer!!.play()
            }
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
        if (radioWave != null) {
            Log.d("Mylog","радио-$radioWave")
            titleTextView.text = radioWave.name
            Picasso.get()
                .load(radioWave.image)
                .into(posterImageView)
            preferencesHelper.setIdPlayMedia(radioWave.id)
        }
    }
//подключаю подсчет станций
    fun createListFragment() {
        fragment = ListFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    searchImageButton.visibility = View.VISIBLE
    titleToolTextView?.text = getString(R.string.list_menu_item)
    //titleToolTextView?.text = items.size.toString()+"-"+getString(R.string.list_menu_item)
initAds()
    Log.d("Mylog","создается список станций")
    }

    private fun createSettingFragment() {
        searchView?.visibility = View.GONE
        titleToolTextView?.visibility=View.VISIBLE
        fragment = SettingFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.set_menu_item)
        initAds()

    }

    private fun createHistoryFragment() {
        searchView?.visibility = View.GONE
        titleToolTextView?.visibility=View.VISIBLE
        fragment = HistoryFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.history_menu_item)
        initAds()
    }

    private fun createFavFragment() {
        searchView?.visibility = View.GONE
        titleToolTextView?.visibility=View.VISIBLE
        fragment = FavoriteFragment().newInstance()
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        titleToolTextView?.text = getString(R.string.fav_menu_item)
        initAds()
    }

    private var bottomNavViewOnItemSelectListener = NavigationBarView.OnItemSelectedListener {
        when (it.itemId) {
            R.id.listFragmentItem -> {
                createListFragment()
                loadPageAds()
                searchImageButton.visibility = View.VISIBLE
                //обновление базы
                updateDb()
            }
            R.id.favoriteFragmentItem -> {
                updateDb()
                createFavFragment()
                loadPageAds()
                searchImageButton.visibility = View.INVISIBLE

            }
            R.id.settingFragmentItem -> {
                createSettingFragment()
                //loadPageAds()
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
//реклама межстраничная
    private fun loadPageAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {

        }
    }
//реклама баннера
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
            requestRecordPermission()
            //setMediaSessionAndVisual()
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

    fun setMediaSessionAndVisual() {
        try {
            mExoPlayer?.let { player ->
                audioSessionId = player.audioSessionId
            }

            mVisualizer?.let { visualizer ->
                audioSessionId?.let { session ->
                    visualizer.setAudioSessionId(session)
                }
            }
        } catch (e: Exception) {
            Log.d("MyLog", "Вылетела ошибка - в setMediaSessionAndVisual")
            mVisualizer?.release()
            audioSessionId?.let { session ->
                mVisualizer?.setAudioSessionId(session)
            }
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
       // titleToolTextView?.text = items.size.toString()+"-"+getString(R.string.list_menu_item)
        titleToolTextView?.text = getString(R.string.list_menu_item)

        searchView = findViewById(R.id.radio_search)
        timerTextView = findViewById(R.id.timerTextView)
        timerImageButton = findViewById(R.id.timerImageButton)
        alertImageButton = findViewById(R.id.alertButton)
        addImageButton = findViewById(R.id.addImageButton)
        searchImageButton = findViewById(R.id.searchImageButton)
        titleTextViewPlayer = findViewById(R.id.titlePlayerTextView)
        trackInfoMiniPlayerTextView = findViewById(R.id.track_info_textView)
        trackInfoMiniPlayerTextView?.isSelected = true

        updateCanceled = getString(R.string.update_canceled)
        newAppIsReady = getString(R.string.new_app_is_ready)
        updateInstall = getString(R.string.update_install)

        textReview = getString(R.string.text_review)
        laiterReview = getString(R.string.laiter_review)
        leaveReview = getString(R.string.leave_review)
        okReview = getString(R.string.ok_review)
        preferenceAlarmHelper = PreferenceAlarmHelper(this)

        val serviceIntent = Intent(this, AlarmRadioPlayerService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "You have already granted this permission!",
                //Toast.LENGTH_LONG).show();
        } else {
            requestRecordPermission();
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
                preferencesHelper.setIdPlayMedia(items[1].id)

            }

            override fun onCancelled(@NonNull @NotNull error: DatabaseError) {}
        }
        database?.addValueEventListener(valueEventListener)

    }


    fun updateDb() {
        database = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference(getString(R.string.firebase_ref))
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull @NotNull snapshot: DataSnapshot) {
                // Создаем список радиостанций, которые будут добавлены в базу данных
                val newRadioWaves = mutableListOf<RadioWave>()

                for (dataSnapshot in snapshot.children) {
                    val radioWave: RadioWave? = dataSnapshot.getValue(RadioWave::class.java)

                    // Если радиостанция уже есть в базе данных, то используем ее данные из базы данных
                    // иначе добавляем новую радиостанцию в список новых радиостанций
                    val existingRadioWave = viewModel.getRadioWaveForId(radioWave?.id)
                    if (existingRadioWave != null) {
                        radioWave?.favorite = existingRadioWave.favorite
                        radioWave?.custom = existingRadioWave.custom
                        radioWave?.countOpen = existingRadioWave.countOpen
                        newRadioWaves.add(radioWave!!)
                    } else {
                        newRadioWaves.add(radioWave!!)
                    }
                }

                // Обновляем базу данных только с новыми радиостанциями
                viewModel.createListRadioWave(newRadioWaves)

                // Устанавливаем флаг "FirstStart" в false, только если данные были успешно получены
                preferencesHelper.setFirstStart(false)
            }

            override fun onCancelled(@NonNull @NotNull error: DatabaseError) {}
        }

        // Запрашиваем данные из Firebase Realtime Database и обновляем их в приложении при каждом изменении
        database!!.addValueEventListener(valueEventListener)
    }








    private fun setMediaItem() {
        //requestRecordPermission()
        val id = preferencesHelper.getIdPlayMedia()
        val url: String?
        try {
            if (mExoPlayer!!.currentMediaItem == null)
              {
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
            //Log.d("Mylog","$mExoPlayer")
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
                "https://www.theaudiodb.com/api/v1/json/523532/search.php?s=${artist[0]}"
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
                "http://mishkindeveloper.download/imageRadio/NoImageSong.jpg"
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
            val er = getString(R.string.error_play_station)
            when (error.errorCode) {

                ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                    titleTextViewPlayer?.visibility = View.INVISIBLE
                    mNameTextView?.visibility = View.INVISIBLE
                    titleTextView.visibility = View.INVISIBLE
                    favoriteImageButton?.visibility = View.INVISIBLE
                    mVisualizer?.visibility = View.INVISIBLE
                    mPosterImageView?.visibility = View.INVISIBLE
                    posterImageView.visibility = View.INVISIBLE
                    animNetLottieAnimationView?.visibility = View.VISIBLE

                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 0 ")

                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.error_network),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Mylog", "Пропажа интернета там где дожна біть анимация")

                    //проверка на наличия интернета
                    chekInternet()

                    //конец проверка на наличия интернета



                }
                ERROR_CODE_IO_FILE_NOT_FOUND -> {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.error_payback),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 1 ")
                }
                ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> {
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 2")
                }
                ERROR_CODE_FAILED_RUNTIME_CHECK ->{
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 3")
                }
                TYPE_REMOTE -> {
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 4 ")
                }
                TYPE_RENDERER -> {
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 5")
                }
                TYPE_SOURCE -> {
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 6")
                }
                TYPE_UNEXPECTED -> {
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 7")
                }
                ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->{
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 8")
                }
                ERROR_CODE_TIMEOUT ->{
                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 9")
                }

                else -> {
                    Toast.makeText(
                        this@MainActivity,
                        er.toString(),
                        Toast.LENGTH_LONG

                    ).show()

                    Log.d("Mylog", "ОШИБКА ЗАПУСКА 10")
                    //chekInternet()

                }
            }

            }
        //проверка на наличия интернета
fun chekInternet(){
            val timerTask = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        Log.d("Mylog", "Попытка запуска радиостанции после отключения интернета")
                        mExoPlayer?.prepare()
                        mExoPlayer?.play()

                        timer.cancel()
                    }
                }
            }
            timer = Timer()
            timer.schedule(timerTask, noDelay, everyFiveSeconds)
            if (mExoPlayer?.isPlaying == true){
                //animNetLottieAnimationView?.visibility = View.INVISIBLE
//                                        timer.cancel()
//                                        timer.purge()
                Log.d("Mylog", "выключится таймер")
            }
}


        //конец проверка на наличия интернета

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            //убрал с основного кода
            //animNetLottieAnimationView?.visibility = View.INVISIBLE
            //Log.d("Mylog","ОШИБКА ЗАПУСКА РАДИОСТАНЦИИ")
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
            titleTextViewPlayer?.visibility = View.VISIBLE
            mNameTextView?.visibility = View.VISIBLE
            titleTextView.visibility = View.VISIBLE
            favoriteImageButton?.visibility = View.VISIBLE
            mVisualizer?.visibility = View.VISIBLE
            mPosterImageView?.visibility = View.VISIBLE
            posterImageView.visibility = View.VISIBLE

            animNetLottieAnimationView?.visibility = View.INVISIBLE

            motionLayout?.transitionToEnd()

            //Log.d("Mylog","открылся еквалайзер")
        } else {
            playImageView?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            //Log.d("Mylog","НЕ!!!!  открылся еквалайзер")
        }
    }

    private fun startPlayerService() {
        val intent = Intent(this, PlayerService::class.java)
        bindService(intent, myConnection, BIND_AUTO_CREATE)
        startService(intent)
    }

    @Deprecated("Deprecated in Java")
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
            createListFragment()

            Toast.makeText(this, R.string.add_radio_station_message, Toast.LENGTH_LONG).show()
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
    }
    private fun requestRecordPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle(R.string.permosion_messege_top)
                .setMessage(R.string.permosion_messege_test)
                .setPositiveButton(
                    "Ok"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_PERMISSION_CODE
                      )

                }
                .setNegativeButton(
                    R.string.permosion_messege_cancel
                ) { dialog, which -> dialog.dismiss() }
                .create().show()

        }
        else
            ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMediaSessionAndVisual()
                //Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_LONG).show()
            } else {
                //requestRecordPermission()
                //Toast.makeText(this, "Permission DENIED", Toast.LENGTH_LONG).show()
            }
        }
    }

    //проверка обновления программы
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            // Handle the update cancellation
            Toast.makeText(this, updateCanceled, Toast.LENGTH_SHORT).show()
        }
    }

    private val installStateUpdatedListener =
        InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // Show the update completion message
                showCompletedUpdate()
            }
        }
    private fun showCompletedUpdate() {
        val snackbar = newAppIsReady?.let {
            Snackbar.make(
                findViewById(android.R.id.content), it,
                Snackbar.LENGTH_INDEFINITE
            )
        }
        snackbar?.setAction(
            updateInstall
        ) { mAppUpdateManager.completeUpdate() }
        snackbar?.show()
    }
    //конец проверки обновления программы

//    private fun checkAlarm() {
//        val alarmIntent = Intent(this, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            this, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE
//        )
//        val isAlarmSet = pendingIntent != null
//
//        if (isAlarmSet) {
//            Log.d("MyLog","Будильник уже запущен")
//            alertImageButton?.setImageResource(R.drawable.baseline_alarm_off_24)
//
//        } else {
//            Log.d("MyLog","Будильник не запуще")
//
//        }

    fun stopAlarmDialogInMain() {
        val yes  = resources.getText(R.string.update_yes)
        val no  = resources.getText(R.string.update_no)
        val title  = resources.getText(R.string.title_alarm_stop)
        val sure  = resources.getText(R.string.text_alarm_stop)
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("$yes"){ _, _ ->
            //stopRadioPlayback()
cancelAlarm()
           // stopAlarmService()
            alarmRadioPlayerService?.stopRadioStation()
            alarmRadioPlayerService?.stopVibration()
            alarmRadioPlayerService?.stopForegroundNotification()
//            val stopAlarmIntent = Intent(this, StopAlarmActivity::class.java)
//            startActivity(stopAlarmIntent)

            //stopRadioStation()


//var alarmReceiver = AlarmReceiver()
//            val stopAlarmIntent = Intent(this, StopAlarmActivity::class.java)
//            stopAlarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            alarmReceiver.onReceive(this,stopAlarmIntent)


            Log.d("MyLog", "Остановился будильник")
            preferenceAlarmHelper.saveBoolean("Alarm",false)
            alertImageButton?.setImageResource(R.drawable.baseline_add_alert_24)

        }
        builder.setNegativeButton("$no"){_, _ -> }
        builder.setTitle("$title")
        builder.setMessage("$sure")
        builder.create().show()
    }


private fun cancelAlarm() {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(this, AlarmReceiver::class.java)
    val alarmPendingIntent = PendingIntent.getBroadcast(
        this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmPendingIntent?.let { pendingIntent ->
        alarmManager.cancel(pendingIntent)
        if (alarmIntent.hasExtra("isAlarmActive") && alarmIntent.getBooleanExtra("isAlarmActive", false)) {
            // Остановить воспроизведение радиостанции
            // stopMediaPlayback()
        }
    }
}

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmRadioPlayerService::class.java)
        stopService(stopIntent)
    }








}

package download.mishkindeveloper.AllRadioUA.services


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_NONE
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.data.entity.Track
import download.mishkindeveloper.AllRadioUA.data.repository.TrackRepository
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import download.mishkindeveloper.AllRadioUA.widget.PlayerWidget
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class PlayerService() : Service(), Parcelable {

    private lateinit var playerBinder: IBinder
    private var mPlayer: ExoPlayer? = null
    private lateinit var playerNotificationManger: PlayerNotificationManager
    private var radioWave: RadioWave? = null
    private var bitMapPoster: Bitmap? = null
    private val mediaSessionTag = "MediaSessionManager"
    private val openAction = "download.mishkindeveloper.AllRadioUA.ACTION_OPEN"
    private val playAction = "download.mishkindeveloper.AllRadioUA.ACTION_PLAY"
    private val pauseAction = "download.mishkindeveloper.AllRadioUA.ACTION_PAUSE"
    private var trackTitle = ""
    private var remoteViews: RemoteViews? = null
    private var thisWidget: ComponentName? = null
    private var appWidgetManager: AppWidgetManager? = null
    private var stationName = ""
    private var artistPoster = ""


    @set:Inject
    internal var trackRepository: TrackRepository? = null

    constructor(parcel: Parcel) : this() {
        playerBinder = parcel.readStrongBinder()
        bitMapPoster = parcel.readParcelable(Bitmap::class.java.classLoader)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return playerBinder
    }


    override fun onCreate() {
        super.onCreate()
        playerBinder = PlayerBinder()
        initPlayer()
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        remoteViews = RemoteViews(applicationContext.packageName, R.layout.player_widget)
        thisWidget = ComponentName(applicationContext, PlayerWidget::class.java)
        remoteViews!!.setOnClickPendingIntent(
            R.id.widgetLinearLayout,
            getPendingSelfIntent(applicationContext, openAction)
        )
    }

    fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        actionEquals(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun actionEquals(intent: Intent?) {
        if (intent?.action.equals(playAction)) {
            mPlayer!!.play()
        }
        if (intent?.action.equals(pauseAction)) {
            mPlayer!!.pause()
        }
        if (intent?.action.equals(openAction)) {
            startActivity()
        }
    }

    private fun initPlayer() {
        mPlayer = ExoPlayer.Builder(this)
            .setUseLazyPreparation(false)
            .setHandleAudioBecomingNoisy(true)
            .setPauseAtEndOfMediaItems(false).build()
        mPlayer!!.addListener(playerListener)
    }

    override fun onDestroy() {
        mPlayer?.release()
    }

    fun getPlayer(): ExoPlayer? {
        return mPlayer
    }

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService? {
            return this@PlayerService
        }
    }

    fun initNotification() {
        // Проверка и запрос разрешения FOREGROUND_SERVICE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                val permissionIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                permissionIntent.data = uri
                permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(permissionIntent)
                return
            }
        }
        playerNotificationManger = PlayerNotificationManager.Builder(
            this, 151,
            this.resources.getString(R.string.app_name)
        )
            .setChannelNameResourceId(R.string.app_name)
            .setChannelImportance(IMPORTANCE_NONE)

            .setMediaDescriptionAdapter(object : MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return radioWave?.name.toString()
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return startPendingActivity()

                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return trackTitle
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: BitmapCallback
                ): Bitmap? {
                    Picasso.get().load(radioWave?.image).into(object : com.squareup.picasso.Target {
                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                            bitMapPoster = BitmapFactory.decodeResource(
                                resources,
                                R.drawable.ic_baseline_music_note_24
                            )
                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                            bitMapPoster = bitmap!!
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    })
                    return bitMapPoster
                }
            }).setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    stopSelf()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(false)
                    }
                }
            }).build()
        playerNotificationManger.setPlayer(mPlayer)
        playerNotificationManger.setSmallIcon(R.drawable.ic_play_icon)
        playerNotificationManger.setUseNextAction(false)
        playerNotificationManger.setUsePreviousAction(true)
        playerNotificationManger.setUsePlayPauseActions(true)
        playerNotificationManger.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        playerNotificationManger.setUseNextActionInCompactView(true)
        playerNotificationManger.setUsePreviousActionInCompactView(false)
        playerNotificationManger.setUseChronometer(true)
        val mediaSession = MediaSessionCompat(this, mediaSessionTag)
        playerNotificationManger.setMediaSessionToken(mediaSession.sessionToken)
        val sessionConnector = MediaSessionConnector(mediaSession)
        sessionConnector.setPlayer(mPlayer)
    }

    private fun startActivity() {
        val i = Intent(this@PlayerService, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(i)

    }

    private fun startPendingActivity(): PendingIntent {
        val i = Intent(this@PlayerService, MainActivity::class.java)
        i.putExtra(getString(R.string.get_serializable_extra), radioWave)
        return PendingIntent.getActivity(
            this@PlayerService, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun setRadioWave(radioWave: RadioWave) {
        this.radioWave = radioWave
        val i = Intent(getString(R.string.intent_filter_notification))
        i.putExtra(getString(R.string.serializable_extra), radioWave)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i);
    }

    fun getRadioWave(): RadioWave? {
        return radioWave
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStrongBinder(playerBinder)
        parcel.writeParcelable(bitMapPoster, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayerService> {
        override fun createFromParcel(parcel: Parcel): PlayerService {
            return PlayerService(parcel)
        }

        override fun newArray(size: Int): Array<PlayerService?> {
            return arrayOfNulls(size)
        }
    }

    private fun loadPoster() {
        Picasso.get()
            .load(radioWave?.image)
            .resize(100, 70)
            .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
                    if (bitmap != null) {
                        remoteViews!!.setImageViewBitmap(R.id.widgetImageView, bitmap)
                    }
                }

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {

                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                }

            })
    }

    private var playerListener = object : Player.Listener {
        @SuppressLint("SimpleDateFormat")
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            if (mediaMetadata.title != null) {
                trackTitle = mediaMetadata.title.toString()
                stationName = mediaMetadata.station.toString()
                remoteViews!!.setTextViewText(R.id.trackWidgetTextView, trackTitle)
                remoteViews!!.setTextViewText(R.id.nameWidgetTextView, stationName)
                loadPoster()
                appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)
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
            trackRepository?.insertTrack(track)
        }

        @SuppressLint("SimpleDateFormat")
        private fun insertTrackAndLoadPoster(mediaMetadata: MediaMetadata, jsonArray: JSONArray) {
            try {
                if (jsonArray.length() > 0) {
                    val artistObject = jsonArray.getJSONObject(0)
                    if (artistObject.has("strArtistFanart")) {
                        val artistPoster = artistObject.getString("strArtistFanart")
                        val track = Track()
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                        val currentDate = sdf.format(Date())
                        track.name = mediaMetadata.title.toString()
                        track.date = currentDate
                        track.image = artistPoster
                        track.station = mediaMetadata.station.toString()
                        trackRepository?.insertTrack(track)
                    } else {
                        insertTrackAndSetDefaultPoster(mediaMetadata)
                    }
                } else {
                    insertTrackAndSetDefaultPoster(mediaMetadata)
                }
            } catch (e: JSONException) {
                // Обработка ошибки при получении данных из JSON массива
                insertTrackAndSetDefaultPoster(mediaMetadata)
            }
        }

        private fun posterRequestOkhttp(mediaMetadata: MediaMetadata) {
            val artist = mediaMetadata.title.toString().split("-")
            val url = "https://www.theaudiodb.com/api/v1/json/523532/search.php?s=${artist[0]}"
            val okHttpClient: OkHttpClient = OkHttpClient()
            val request: Request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Обработка ошибки при выполнении запроса
                    insertTrackAndSetDefaultPoster(mediaMetadata)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        // Обработка ошибки при получении ответа
                        insertTrackAndSetDefaultPoster(mediaMetadata)
                        return
                    }

                    try {
                        val json = JSONObject(responseBody)
                        val jsonArray = json.getJSONArray("artists")
                        insertTrackAndLoadPoster(mediaMetadata, jsonArray)
                    } catch (e: JSONException) {
                        // Обработка ошибки при разборе JSON
                        insertTrackAndSetDefaultPoster(mediaMetadata)
                    }
                }
            })
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            remoteViews?.let { views ->
                val playPauseIcon = if (isPlaying) {
                    R.drawable.ic_baseline_pause_24
                } else {
                    R.drawable.ic_baseline_play_arrow_24
                }

                views.setImageViewResource(R.id.playWidgetImageButton, playPauseIcon)

                val pendingIntent = if (isPlaying) {
                    getPendingSelfIntent(applicationContext, pauseAction)
                } else {
                    getPendingSelfIntent(applicationContext, playAction)
                }
                views.setOnClickPendingIntent(R.id.playWidgetImageButton, pendingIntent)

                appWidgetManager?.updateAppWidget(thisWidget, views)
            }
        }

    }

}
package download.mishkindeveloper.AllRadioUA.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.squareup.picasso.Picasso
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave
import download.mishkindeveloper.AllRadioUA.enums.DisplayListType
import download.mishkindeveloper.AllRadioUA.listeners.MenuItemIdListener
import download.mishkindeveloper.AllRadioUA.services.PlayerService
import download.mishkindeveloper.AllRadioUA.ui.listFragment.ListFragment
import download.mishkindeveloper.AllRadioUA.ui.listFragment.adapter.WaveViewHolder
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import java.util.*


class RadioStationAdapter(
    private var items: List<RadioWave>,
    var context: Context?,
    private var mPlayerService: PlayerService? ,
    private var menuItemIdListener: MenuItemIdListener?

) :
    RecyclerView.Adapter<WaveViewHolder>() {
    private val grid = 0
    private val list = 1
    private val waveViewHolder: WaveViewHolder? = null
    private  var displayListType: DisplayListType?=null
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var selectedRadioStation: RadioWave? = null
    private var alarmHour: Int = 0
    private var alarmMinute: Int = 0
    private lateinit var fragment: AlarmFragment
    private lateinit var preferenceAlarmHelper: PreferenceAlarmHelper
    //private lateinit var alertImageButton : ImageButton
    private var alarmPendingIntent: PendingIntent? = null
    private var alertImageButton: ImageButton? = null
    fun setSelectedRadioStation(position: Int) {
        selectedPosition = position
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayListType== DisplayListType.List){
            return list
        } else{
            return grid
        }
    }

    fun setDisplayListType(displayListType: DisplayListType) {
        this.displayListType = displayListType
    }

    fun setItems(items: List<RadioWave>) {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaveViewHolder {
        val layoutInflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        when (viewType) {
            //переключение отображения радиостаций
            list -> {
                return WaveViewHolder(
                    layoutInflater.inflate(
                        R.layout.alert_radio_station,
                        parent,
                        false
                    )
                )
            }
            grid -> {
                return WaveViewHolder(
                    layoutInflater.inflate(
                        R.layout.alarm_wave_items_grid,
                        parent,
                        false
                    )
                )
            }
        }
        return waveViewHolder!!
    }


    override fun onBindViewHolder(holder: WaveViewHolder, position: Int) {
        holder.frequencyTextView?.text = items[position].fmFrequency
        holder.nameTextView?.text = items[position].name
        preferenceAlarmHelper = PreferenceAlarmHelper(context!!)

        checkImageNull(position, holder)
        checkFavItem(position, holder)
        checkCustomItem(position, holder)

        holder.menuImageButton?.setOnClickListener {
            menuItemIdListener?.getItemMenu(items[position].id)
        }

        // нажатие на радио элемент
        holder.itemView.setOnClickListener {
            //alertImageButton = holder.itemView.findViewById(R.id.alertButton)

            //setSelectedRadioStation(items[position])
            menuItemIdListener?.updateCountOpenItem(items[position].id)
            startAlarm(items[position].url) // Передаем адрес станции
            fragment = AlarmFragment()
            preferenceAlarmHelper.saveBoolean("Alarm",true)
            val alertTextSet = context!!.getText(R.string.alarm_set)
            Toast.makeText(context,alertTextSet,Toast.LENGTH_LONG).show()
            alertImageButton?.setImageResource(R.drawable.baseline_set_alert_24)
            startMainActivity()

        }

        holder.setAlarmImageButton?.setOnClickListener {
            //setSelectedRadioStation(items[position])
            menuItemIdListener?.updateCountOpenItem(items[position].id)
            //startAlarm(items[position].url)
            preferenceAlarmHelper.saveBoolean("Alarm",true)
            val alertTextSet = context!!.getText(R.string.alarm_set)
            Toast.makeText(context,alertTextSet,Toast.LENGTH_LONG).show()
            alertImageButton?.setImageResource(R.drawable.baseline_set_alert_24)
            startMainActivity()

        }

        radioWaveNameEquals(position, holder)
    }

    fun setSelectedRadioStation(radioWave: RadioWave?) {
        selectedRadioStation = radioWave
    }

    private fun startAlarm(radioStationUrl: String?) {
        if (radioStationUrl.isNullOrEmpty()) {
            return
        }

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("radioStation", radioStationUrl)
        }

        alarmPendingIntent = PendingIntent.getBroadcast(
            context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance()
        val sharedPreferences = context?.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
        alarmHour = sharedPreferences?.getInt("AlarmHour", -1)!!
        alarmMinute = sharedPreferences?.getInt("AlarmMinute", -1)!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, alarmHour)
        calendar.set(Calendar.MINUTE, alarmMinute)
        calendar.set(Calendar.SECOND, 0)


        // Получите AlarmManager
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Установите повторяющийся будильник с заданным временем
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmPendingIntent
        )
        alarmIntent.putExtra("isAlarmActive", true)
Log.d("MyLog","alarmintent - $alarmIntent")
Log.d("MyLog","alarmhour - $alarmHour")
Log.d("MyLog","alarmminute - $alarmMinute")

    }


    fun clearItems(){
        items.isNullOrEmpty()
    }

    private fun checkCustomItem(position: Int, holder: WaveViewHolder) {
        if (items[position].custom == false) {
            holder.menuImageButton?.visibility = View.GONE
        } else {
            holder.menuImageButton?.visibility = View.VISIBLE
        }
    }

    private fun checkFavItem(position: Int, holder: WaveViewHolder) {
        if (items[position].favorite == true) {
            holder.favImageView?.visibility = View.VISIBLE

        } else {
            holder.favImageView?.visibility = View.INVISIBLE
        }
    }

    private fun checkImageNull(position: Int, holder: WaveViewHolder) {
        if (TextUtils.isEmpty(items[position].image)) {
            holder.imageViewWave?.setImageResource(R.mipmap.ic_launcher_round);
        } else {
            Picasso.get()
                .load(items[position].image)
                .into(holder.imageViewWave)

        }
    }
    //видимость анимации
    private fun radioWaveNameEquals(position: Int, holder: WaveViewHolder) {

        if (mPlayerService?.getRadioWave()?.name?.toString().equals(items[position].name)) {
            holder.lottieAnimationView?.visibility = View.VISIBLE
        } else {
            holder.lottieAnimationView?.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setAlarmTime(hour: Int, minute: Int) {
        alarmHour = hour
        alarmMinute = minute
    }
    interface RadioStationSelectedListener {
        fun onRadioStationSelected(radioWave: RadioWave)
        fun onRadioStationSelectionCanceled()
    }

    fun startMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }

}


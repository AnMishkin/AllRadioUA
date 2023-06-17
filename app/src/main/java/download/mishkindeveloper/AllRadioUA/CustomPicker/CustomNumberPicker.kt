package download.mishkindeveloper.AllRadioUA.сustomPicker

import download.mishkindeveloper.AllRadioUA.R
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


class CustomNumberPicker : NumberPicker {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(context: Context?) : super(context) {
        init()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun init() {
        // Настройте внешний вид NumberPicker здесь
        // Например, установите цвета фона и текста
        this.setBackgroundColor(ContextCompat.getColor(context, R.color.picker_back))
        this.textColor = ContextCompat.getColor(context, R.color.yellow)
    }

}

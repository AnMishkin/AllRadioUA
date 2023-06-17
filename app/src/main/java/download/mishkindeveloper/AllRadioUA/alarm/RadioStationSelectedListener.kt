package download.mishkindeveloper.AllRadioUA.alarm

import download.mishkindeveloper.AllRadioUA.data.entity.RadioWave

interface RadioStationSelectedListener {
    fun onRadioStationSelected(radioStation: RadioWave)
    fun onRadioStationSelectionCanceled()
}
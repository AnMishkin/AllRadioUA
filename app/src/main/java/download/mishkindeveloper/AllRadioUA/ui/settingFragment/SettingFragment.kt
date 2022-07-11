package download.mishkindeveloper.AllRadioUA.ui.settingFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import download.mishkindeveloper.AllRadioUA.R
import download.mishkindeveloper.AllRadioUA.enums.DisplayListType
import download.mishkindeveloper.AllRadioUA.helper.PreferenceHelper
import download.mishkindeveloper.AllRadioUA.ui.main.MainActivity
import download.mishkindeveloper.AllRadioUA.ui.main.MainViewModel
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class SettingFragment : Fragment() {
    private var displayTypeRadioGroup: RadioGroup? = null
    private var updateStationTextView: TextView? = null
    private var clearHistoryTextView: TextView? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var preferencesHelper: PreferenceHelper
    private var updateLottieAnimView: LottieAnimationView? = null
    private var trashLottieAnimationView: LottieAnimationView? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setting_fragment, container, false)
    }

    private fun init(view: View) {
        updateLottieAnimView = view.findViewById(R.id.lottieAnimUpdateDb)
        displayTypeRadioGroup = view.findViewById(R.id.displayTypeRadioGroup)
        trashLottieAnimationView = view.findViewById(R.id.lottieAnimTrash)
        clearHistoryTextView = view.findViewById(R.id.clearHistoryTextView)
        updateStationTextView = view.findViewById(R.id.updateStationTextView)
    }

    private fun loadDisplayListType() {
        val displayListType = preferencesHelper.getDisplayListType()
        if (displayListType == DisplayListType.List) {
            displayTypeRadioGroup?.check(R.id.listRadioButton)
        } else {
            displayTypeRadioGroup?.check(R.id.gridRadioButton)
        }
    }

    private fun setListeners() {
        displayTypeRadioGroup?.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.listRadioButton -> {
                    preferencesHelper.setDisplayListType(DisplayListType.List)
                }
                R.id.gridRadioButton -> {
                    preferencesHelper.setDisplayListType(DisplayListType.Grid)
                }
            }
        }
        updateLottieAnimView?.setOnClickListener { updateDbAndAnim() }
        trashLottieAnimationView?.setOnClickListener { clearHistoryAndAnim() }
        clearHistoryTextView?.setOnClickListener { clearHistoryAndAnim() }
        updateStationTextView?.setOnClickListener { updateDbAndAnim() }
    }

    private fun clearHistoryAndAnim() {
        trashLottieAnimationView?.playAnimation()
        viewModel.deleteAllHistory()
        Toast.makeText(context, getString(R.string.history_clear_toast), Toast.LENGTH_SHORT)
            .show()
    }

    private fun updateDbAndAnim() {
        updateLottieAnimView?.playAnimation()
        (activity as MainActivity?)?.updateDb()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        loadDisplayListType()
        setListeners()
    }

    companion object

    fun newInstance(): SettingFragment {
        return SettingFragment()
    }
}
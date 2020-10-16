package gr.blackswamp.diceroller.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.ActivityMainBinding
import gr.blackswamp.diceroller.logic.MainViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent

class MainActivity : AppCompatActivity(), KoinComponent {
    private val vm by viewModel<MainViewModel>()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    //<editor-fold desc="view bindings">
    private val base by lazy { binding.base }
    private var snackbar: Snackbar? = null
    //</editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        vm.error.observe(this, this::showError)
    }

    private fun showError(message: String?) {
        snackbar?.dismiss()
        if (message == null)
            return
        snackbar = Snackbar.make(base, message, Snackbar.LENGTH_LONG)
            .also { it.show() }
    }

//    fun showHelp() {
//        val prompt = MaterialTapTargetPrompt.Builder(this)
//            .setTarget(findViewById(R.id.action1))
//            .setPrimaryText("primary text")
//            .setAutoDismiss(false)
//            .setPromptBackground(FullscreenPromptBackground())
//            .setSecondaryText("secondary text")
//            .setPromptStateChangeListener { prompt, state ->
//                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
//                    Toast.makeText(this, "Yay", Toast.LENGTH_SHORT).show()
//                    prompt.dismiss()
//                }
//            }.create()
//        prompt!!.show()
//    }

}
package gr.blackswamp.diceroller.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.ActivityMainBinding
import gr.blackswamp.diceroller.logic.MainViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val vm by viewModels<MainViewModel>()
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
}

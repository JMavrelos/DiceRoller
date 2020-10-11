package gr.blackswamp.diceroller.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.MainActivityBinding
import gr.blackswamp.diceroller.logic.MainViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val vm by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        setUpListeners()
        setUpObservers()
    }

    private fun setUpListeners() {

        binding.d4.setOnClickListener { vm.addRoll(4) }
        binding.d6.setOnClickListener { vm.addRoll(6) }
        binding.d8.setOnClickListener { vm.addRoll(8) }
        binding.d10.setOnClickListener { vm.addRoll(10) }
        binding.d12.setOnClickListener { vm.addRoll(12) }
        binding.d20.setOnClickListener { vm.addRoll(20) }
        binding.d4times.setOnClickListener { vm.clearRoll(4) }
        binding.d6times.setOnClickListener { vm.clearRoll(6) }
        binding.d8times.setOnClickListener { vm.clearRoll(8) }
        binding.d10times.setOnClickListener { vm.clearRoll(10) }
        binding.d12times.setOnClickListener { vm.clearRoll(12) }
        binding.d20times.setOnClickListener { vm.clearRoll(20) }
        binding.save.setOnClickListener { vm.saveSet() }
        binding.delete.setOnClickListener { vm.deleteSet() }
    }


    private fun setUpObservers() {

    }

    private fun <D> LiveData<D>.observe(observer: ((D?) -> Unit)) {
        this.observe(this@MainActivity, {
            observer.invoke(it)
        })
    }
}
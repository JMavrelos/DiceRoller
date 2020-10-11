package gr.blackswamp.diceroller.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.databinding.MainActivityBinding
import gr.blackswamp.diceroller.logic.MainViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val vm by viewModel<MainViewModel>()

    //<editor-fold desc="view bindings">
    private val times by lazy { binding.times }
    private val sets by lazy { binding.sets }
    private val rolls by lazy { binding.rolls }
    private val d4 by lazy { binding.d4 }
    private val d6 by lazy { binding.d6 }
    private val d8 by lazy { binding.d8 }
    private val d10 by lazy { binding.d10 }
    private val d12 by lazy { binding.d12 }
    private val d20 by lazy { binding.d20 }
    private val d4times by lazy { binding.d4times }
    private val d6times by lazy { binding.d6times }
    private val d8times by lazy { binding.d8times }
    private val d10times by lazy { binding.d10times }
    private val d12times by lazy { binding.d12times }
    private val d20times by lazy { binding.d20times }
    private val timesGroup by lazy { binding.times }
    private val clear by lazy { binding.clear }
    private val action by lazy { binding.action }
    private val setAdapter by lazy { SetAdapter(vm::selectSet) }
    private val rollAdapter by lazy { RollAdapter() }
    //</editor-fold>


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        rolls.adapter = rollAdapter
        sets.adapter = setAdapter
        setUpListeners()
        setUpObservers()
    }

    private fun setUpListeners() {
        d4.setOnClickListener { vm.roll(Die.D4) }
        d6.setOnClickListener { vm.roll(Die.D6) }
        d8.setOnClickListener { vm.roll(Die.D8) }
        d10.setOnClickListener { vm.roll(Die.D10) }
        d12.setOnClickListener { vm.roll(Die.D12) }
        d20.setOnClickListener { vm.roll(Die.D20) }
        d4times.setOnClickListener { vm.clear(Die.D4) }
        d6times.setOnClickListener { vm.clear(Die.D6) }
        d8times.setOnClickListener { vm.clear(Die.D8) }
        d10times.setOnClickListener { vm.clear(Die.D10) }
        d12times.setOnClickListener { vm.clear(Die.D12) }
        d20times.setOnClickListener { vm.clear(Die.D20) }
        clear.setOnClickListener { vm.clear() }
        action.setOnClickListener { vm.action() }
        rolls.setOnClickListener { vm.clearRolls() }
    }


    private fun setUpObservers() {
        vm.state.observe(this, this::updateState)
        vm.sets.observe(this, setAdapter::submit)
    }


    private fun updateState(state: MainActivityState?) {
        if (state == null)
            return
        rollAdapter.submit(state.rolls)

        timesGroup.isVisible = state.set != null

        if (state.set == null) {
            d4times.text = "0"
            d6times.text = "0"
            d8times.text = "0"
            d10times.text = "0"
            d12times.text = "0"
            d20times.text = "0"
            action.setImageResource(R.drawable.ic_group)
            clear.visibility = View.INVISIBLE
        } else {
            d4times.text = (state.set.dice[Die.D4] ?: 0).toString()
            d6times.text = (state.set.dice[Die.D6] ?: 0).toString()
            d8times.text = (state.set.dice[Die.D8] ?: 0).toString()
            d10times.text = (state.set.dice[Die.D10] ?: 0).toString()
            d12times.text = (state.set.dice[Die.D12] ?: 0).toString()
            d20times.text = (state.set.dice[Die.D20] ?: 0).toString()
            action.setImageResource(R.drawable.ic_save)
            clear.visibility = View.VISIBLE
            if (state.editing)
                clear.setImageResource(R.drawable.ic_delete)
            else
                clear.setImageResource(R.drawable.ic_cancel)
        }

    }

}
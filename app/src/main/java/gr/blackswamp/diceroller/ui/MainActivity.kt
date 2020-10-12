package gr.blackswamp.diceroller.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
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
    private val sets by lazy { binding.sets }
    private val rolls by lazy { binding.rolls }
    private val d4 by lazy { binding.d4 }
    private val d6 by lazy { binding.d6 }
    private val d8 by lazy { binding.d8 }
    private val d10 by lazy { binding.d10 }
    private val d12 by lazy { binding.d12 }
    private val d20 by lazy { binding.d20 }
    private val d100 by lazy { binding.d100 }
    private val d4number by lazy { binding.d4number }
    private val d6number by lazy { binding.d6number }
    private val d8number by lazy { binding.d8number }
    private val d10number by lazy { binding.d10number }
    private val d12number by lazy { binding.d12number }
    private val d20number by lazy { binding.d20number }
    private val modNumber by lazy { binding.modNumber }
    private val timesGroup by lazy { binding.numberGroup }
    private val film by lazy { binding.film }
    private val help by lazy { binding.help }
    private val action1 by lazy { binding.action1 }
    private val action2 by lazy { binding.action2 }
    private val action3 by lazy { binding.action3 }
    private val setAdapter by lazy { SetAdapter(vm::rollSet, vm::editSet) }
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
        d4number.value.setOnClickListener { vm.clear(Die.D4) }
        d6number.value.setOnClickListener { vm.clear(Die.D6) }
        d8number.value.setOnClickListener { vm.clear(Die.D8) }
        d10number.value.setOnClickListener { vm.clear(Die.D10) }
        d12number.value.setOnClickListener { vm.clear(Die.D12) }
        d20number.value.setOnClickListener { vm.clear(Die.D20) }
        modNumber.value.setOnClickListener { vm.clear(Die.Mod) }

        d4number.add.setOnClickListener { vm.change(Die.D4, true) }
        d6number.add.setOnClickListener { vm.change(Die.D6, true) }
        d8number.add.setOnClickListener { vm.change(Die.D8, true) }
        d10number.add.setOnClickListener { vm.change(Die.D10, true) }
        d12number.add.setOnClickListener { vm.change(Die.D12, true) }
        d20number.add.setOnClickListener { vm.change(Die.D20, true) }
        modNumber.add.setOnClickListener { vm.change(Die.Mod, true) }

        d4number.remove.setOnClickListener { vm.change(Die.D4, false) }
        d6number.remove.setOnClickListener { vm.change(Die.D6, false) }
        d8number.remove.setOnClickListener { vm.change(Die.D8, false) }
        d10number.remove.setOnClickListener { vm.change(Die.D10, false) }
        d12number.remove.setOnClickListener { vm.change(Die.D12, false) }
        d20number.remove.setOnClickListener { vm.change(Die.D20, false) }
        modNumber.remove.setOnClickListener { vm.change(Die.Mod, false) }

        action1.setOnClickListener { vm.action1() }
        action2.setOnClickListener { vm.action2() }
        action3.setOnClickListener { vm.action3() }

        d4.setOnClickListener { vm.roll(Die.D4) }
        d6.setOnClickListener { vm.roll(Die.D6) }
        d8.setOnClickListener { vm.roll(Die.D8) }
        d10.setOnClickListener { vm.roll(Die.D10) }
        d12.setOnClickListener { vm.roll(Die.D12) }
        d20.setOnClickListener { vm.roll(Die.D20) }
        d100.setOnClickListener { vm.roll(Die.Mod) }

        help.setOnClickListener { vm.pleaseHelpMe() }
        film.setOnClickListener { vm.clearRolls() }
    }


    private fun setUpObservers() {
        vm.state.observe(this, this::updateState)
        vm.sets.observe(this, setAdapter::submit)
    }


    private fun updateState(state: MainActivityState?) {
        if (state == null)
            return
        rollAdapter.submit(state.rolls)
        setAdapter.setSelected(state.set?.id)
        updateVisibility(timesGroup, state.set == null)

        if (state.set == null) { //rolling normally
            updateIcon(action1, R.drawable.ic_add)
            updateVisibility(action2, false)
            updateVisibility(action3, false)
            updateVisibility(modNumber.root, false)
            updateVisibility(timesGroup, false)
            updateEnabled(d4, true)
            updateEnabled(d6, true)
            updateEnabled(d8, true)
            updateEnabled(d10, true)
            updateEnabled(d12, true)
            updateEnabled(d20, true)
            updateEnabled(d100, true)
            updateText(d100, getString(R.string.d100))
            updateAlpha(film, 0f)
        } else if (!state.editing) { //new set
            updateIcon(action1, R.drawable.ic_save)
            updateIcon(action2, R.drawable.ic_cancel)
            updateVisibility(action2, true)
            updateVisibility(action3, false)
            updateVisibility(modNumber.root, true)
            updateVisibility(timesGroup, true)
            updateEnabled(d4, false)
            updateEnabled(d6, false)
            updateEnabled(d8, false)
            updateEnabled(d10, false)
            updateEnabled(d12, false)
            updateEnabled(d20, false)
            updateEnabled(d100, false)
            updateText(d100, getString(R.string.modifier))
            updateAlpha(film, 0.1f)
        } else {//editing a set
            updateIcon(action1, R.drawable.ic_save)
            updateIcon(action2, R.drawable.ic_delete)
            updateIcon(action3, R.drawable.ic_cancel)
            updateVisibility(action2, true)
            updateVisibility(action3, true)
            updateVisibility(modNumber.root, true)
            updateVisibility(timesGroup, true)
            updateEnabled(d4, false)
            updateEnabled(d6, false)
            updateEnabled(d8, false)
            updateEnabled(d10, false)
            updateEnabled(d12, false)
            updateEnabled(d20, false)
            updateEnabled(d100, false)
            updateText(d100, getString(R.string.modifier))
            updateAlpha(film, 0.1f)
        }
        updateValues(state.set)
    }

    private fun updateVisibility(view: View, visible: Boolean) {
        if (view.isVisible && !visible)
            view.isVisible = false
        else if (!view.isVisible && visible)
            view.isVisible = true

    }

    private fun updateAlpha(view: View, alpha: Float) {
        if (view.alpha != alpha) {
            view.alpha = alpha
        }
    }

    private fun updateIcon(imageView: ImageView, @DrawableRes resId: Int) {
        val current = imageView.tag as? Int
        if (current != resId) {
            imageView.tag = resId
            imageView.setImageResource(resId)
        }
    }

    private fun updateValues(dieSet: DieSet?) {
        updateText(d4number.value, dieSet?.dice?.get(Die.D4)?.toString())
        updateText(d6number.value, dieSet?.dice?.get(Die.D6)?.toString())
        updateText(d8number.value, dieSet?.dice?.get(Die.D8)?.toString())
        updateText(d10number.value, dieSet?.dice?.get(Die.D10)?.toString())
        updateText(d12number.value, dieSet?.dice?.get(Die.D12)?.toString())
        updateText(d20number.value, dieSet?.dice?.get(Die.D20)?.toString())
        updateText(modNumber.value, dieSet?.dice?.get(Die.Mod)?.toString())
    }

    private fun updateText(textView: TextView, text: String?) {
        if (textView.text != text)
            textView.text = text
    }

    private fun updateEnabled(view: View, enabled: Boolean) {
        if (view.isEnabled && !enabled)
            view.isEnabled = false
        else if (!view.isEnabled && enabled)
            view.isEnabled = true
    }

}
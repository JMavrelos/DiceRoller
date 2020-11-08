package gr.blackswamp.diceroller.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import gr.blackswamp.diceroller.R
import gr.blackswamp.diceroller.core.widget.*
import gr.blackswamp.diceroller.databinding.FragmentHomeBinding
import gr.blackswamp.diceroller.logic.FragmentParent
import gr.blackswamp.diceroller.logic.HomeViewModel
import gr.blackswamp.diceroller.logic.MainViewModel
import gr.blackswamp.diceroller.ui.adapters.RollAdapter
import gr.blackswamp.diceroller.ui.adapters.SetAdapter
import gr.blackswamp.diceroller.ui.dialogs.NameDialog
import gr.blackswamp.diceroller.ui.model.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf

class HomeFragment : Fragment(), KoinComponent {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private val parent: FragmentParent by sharedViewModel<MainViewModel>()
    private val vm by viewModel<HomeViewModel> { parametersOf(parent) }

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
    private val numberGroup by lazy { binding.numberGroup }
    private val dieGroup: Group2 by lazy { binding.dieGroup }
    private val help by lazy { binding.help }
    private val action1 by lazy { binding.action1 }
    private val action2 by lazy { binding.action2 }
    private val action3 by lazy { binding.action3 }
    private val setAdapter by lazy { SetAdapter({ vm.process(HomeEvent.RollSet(it)) }, { vm.process(HomeEvent.EditSet(it)) }) }
    private val rollAdapter by lazy { RollAdapter() }
    private val inPortrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
    //</editor-fold>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(view)
        setUpListeners()
        setUpObservers()
    }

    private fun initView(view: View) {
        val layoutManager = FlexboxLayoutManager(view.context)
        layoutManager.flexDirection = FlexDirection.ROW
        rolls.layoutManager = layoutManager
        rolls.adapter = rollAdapter
        sets.adapter = setAdapter
    }

    private fun setUpListeners() {
        d4number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D4)) }
        d6number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D6)) }
        d8number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D8)) }
        d10number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D10)) }
        d12number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D12)) }
        d20number.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D20)) }
        modNumber.value.setOnClickListener { vm.process(HomeEvent.Clear(Die.D100)) }

        d4number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D4)) }
        d6number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D6)) }
        d8number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D8)) }
        d10number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D10)) }
        d12number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D12)) }
        d20number.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D20)) }
        modNumber.add.setOnClickListener { vm.process(HomeEvent.Increase(Die.D100)) }

        d4number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D4)) }
        d6number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D6)) }
        d8number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D8)) }
        d10number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D10)) }
        d12number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D12)) }
        d20number.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D20)) }
        modNumber.remove.setOnClickListener { vm.process(HomeEvent.Decrease(Die.D100)) }

        action1.setOnClickListener { vm.process(HomeEvent.Action1) }
        action2.setOnClickListener { vm.process(HomeEvent.Action2) }
        action3.setOnClickListener { vm.process(HomeEvent.Action3) }

        d4.setOnClickListener { vm.process(HomeEvent.Roll(Die.D4)) }
        d6.setOnClickListener { vm.process(HomeEvent.Roll(Die.D6)) }
        d8.setOnClickListener { vm.process(HomeEvent.Roll(Die.D8)) }
        d10.setOnClickListener { vm.process(HomeEvent.Roll(Die.D10)) }
        d12.setOnClickListener { vm.process(HomeEvent.Roll(Die.D12)) }
        d20.setOnClickListener { vm.process(HomeEvent.Roll(Die.D20)) }
        d100.setOnClickListener { vm.process(HomeEvent.Roll(Die.D100)) }

        help.setOnClickListener { vm.process(HomeEvent.Help) }

        setFragmentResultListener(NameDialog.REQUEST_ID, this::dialogFinished)
    }

    private fun setUpObservers() {
        vm.state.observe(viewLifecycleOwner, this::updateState)
        vm.sets.observe(viewLifecycleOwner, setAdapter::submit)
        vm.effect.observe(viewLifecycleOwner, this::newEffect)
    }


    private fun newEffect(effect: HomeEffect) {
        when (effect) {
            is HomeEffect.ShowHelp -> findNavController().navigate(HomeFragmentDirections.showHelp())
            is HomeEffect.ShowNameDialog -> findNavController().navigate(HomeFragmentDirections.showNameInput(effect.nextId))
            is HomeEffect.ShowError -> parent.showError(effect.id)
        }
    }

    private fun dialogFinished(key: String, bundle: Bundle) {
        if (key == NameDialog.REQUEST_ID) {
            val name = bundle.getString(NameDialog.RESULT_NAME)
                ?: return
            vm.process(HomeEvent.NameSelected(name))
        }
    }

    private fun updateState(state: HomeState) {
        dieGroup.enabled = state is HomeState.Viewing
        numberGroup.visible = state !is HomeState.Viewing
        modNumber.root.visible = state !is HomeState.Viewing
        when (state) {
            is HomeState.Viewing -> {
                rollAdapter.submit(state.rolls)
                setAdapter.setSelected(null)
                updateAction(action1, R.drawable.ic_add, true)
                updateAction(action2, -1, false)
                updateAction(action3, -1, false)
                d100.res = R.string.d100
            }
            is HomeState.Creating -> {
                rollAdapter.submit(listOf())
                setAdapter.setSelected(null)
                updateAction(action1, R.drawable.ic_save, true)
                updateAction(action2, R.drawable.ic_cancel, true)
                updateAction(action3, -1, false)
                d100.res = R.string.modifier
                updateValues(state.set)
            }
            is HomeState.Editing -> {
                rollAdapter.submit(listOf())
                setAdapter.setSelected(state.set.id)
                updateAction(action1, R.drawable.ic_save, true)
                updateAction(action2, R.drawable.ic_delete, true)
                updateAction(action3, R.drawable.ic_cancel, true)
                d100.res = R.string.modifier
                updateValues(state.set)
            }
        }
    }

    private fun updateAction(imageView: ImageView, @DrawableRes resId: Int, visible: Boolean) {
        if (resId != -1) {
            val current = imageView.tag as? Int
            if (current != resId) {
                imageView.tag = resId
                imageView.setImageResource(resId)
            }
        }
        if (!imageView.isVisible && visible) {
            if (inPortrait) {
                imageView.translationX = (imageView.left - action1.left) * -1f
                imageView.isVisible = true
                imageView.animate()
                    .translationX(0f)
            } else {
                imageView.translationY = (imageView.bottom - action1.bottom) * -1f
                imageView.isVisible = true
                imageView.animate()
                    .translationY(0f)
            }
        } else if (imageView.isVisible && !visible) {
            if (inPortrait) {
                imageView.animate()
                    .translationX((imageView.left - action1.left) * -1f)
                    .withEndAction {
                        imageView.isVisible = false
                        imageView.translationX = 0f
                    }
            } else {
                imageView.animate()
                    .translationY((imageView.bottom - action1.bottom) * -1f)
                    .withEndAction {
                        imageView.isVisible = false
                        imageView.translationY = 0f
                    }
            }

        }
    }

    private fun updateValues(dieSet: DieSet?) {
        d4number.value.value = dieSet?.dice?.get(Die.D4)?.toString() ?: ""
        d6number.value.value = dieSet?.dice?.get(Die.D6)?.toString() ?: ""
        d8number.value.value = dieSet?.dice?.get(Die.D8)?.toString() ?: ""
        d10number.value.value = dieSet?.dice?.get(Die.D10)?.toString() ?: ""
        d12number.value.value = dieSet?.dice?.get(Die.D12)?.toString() ?: ""
        d20number.value.value = dieSet?.dice?.get(Die.D20)?.toString() ?: ""
        modNumber.value.value = dieSet?.modifier?.toString() ?: ""
    }
}
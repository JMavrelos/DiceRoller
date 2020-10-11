package gr.blackswamp.diceroller.app

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import gr.blackswamp.diceroller.data.MainRepository
import gr.blackswamp.diceroller.logic.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val applicationModule = module {
    single<SharedPreferences> {
        androidApplication().getSharedPreferences(
            "DiceRollerPrefs",
            MODE_PRIVATE
        )
    }

    //<editor-fold desc="repositories">
    single { MainRepository() }
    //</editor-fold>

    //<editor-fold desc="viewModels">
    viewModel { MainViewModel(androidApplication()) }
    //</editor-fold>
}
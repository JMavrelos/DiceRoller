package gr.blackswamp.diceroller.app

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.repos.MainRepository
import gr.blackswamp.diceroller.logic.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val applicationModule = module {
    single<SharedPreferences> {
        androidApplication().getSharedPreferences(
            "DiceRollerPrefs",
            MODE_PRIVATE
        )
    }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "data.db")
            .build()
    }

    //<editor-fold desc="repositories">
    single { MainRepository() }
    //</editor-fold>

    //<editor-fold desc="viewModels">
    viewModel { MainViewModel(androidApplication()) }
    //</editor-fold>
}
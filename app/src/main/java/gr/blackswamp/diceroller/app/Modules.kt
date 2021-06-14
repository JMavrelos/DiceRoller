package gr.blackswamp.diceroller.app

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gr.blackswamp.diceroller.data.db.AppDatabase
import gr.blackswamp.diceroller.data.rnd.RandomGenerator
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideRandomGenerator(): RandomGenerator {
        return RandomGenerator()
    }

    @Provides
    @Singleton
    fun providePreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences(
            "DiceRollerPrefs",
            MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "data.db")
            .build()
    }
}
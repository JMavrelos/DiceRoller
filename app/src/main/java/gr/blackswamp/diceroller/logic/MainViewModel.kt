package gr.blackswamp.diceroller.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import gr.blackswamp.diceroller.core.livedata.LiveEvent

class MainViewModel(app: Application) : AndroidViewModel(app), FragmentParent {
    private val _error = LiveEvent<String>()
    val error: LiveData<String> = _error

    override fun showError(message: String) =
        _error.postValue(message)

    override fun showError(id: Int) {
        _error.postValue(getApplication<Application>().getString(id))
    }
}
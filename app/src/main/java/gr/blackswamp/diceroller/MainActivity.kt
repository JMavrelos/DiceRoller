package gr.blackswamp.diceroller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import gr.blackswamp.diceroller.databinding.MainActivityBinding
import gr.blackswamp.diceroller.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    private val binding by lazy { MainActivityBinding.inflate(layoutInflater) }
    private val vm by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}
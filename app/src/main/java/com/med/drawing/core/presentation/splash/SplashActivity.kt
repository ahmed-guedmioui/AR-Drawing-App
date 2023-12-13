package com.med.drawing.core.presentation.splash

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.R
import com.med.drawing.core.domain.usecase.ads.InterManager
import com.med.drawing.core.presentation.home.HomeActivity
import com.med.drawing.core.presentation.tips.TipsActivity
import com.med.drawing.databinding.ActivitySplashBinding
import com.med.drawing.util.AppDataResult
import com.med.drawing.util.AppAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    private lateinit var splashState: SplashState
    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        AppAnimation().startRepeatingAnimation(binding.animationImage)

        lifecycleScope.launch {
            splashViewModel.splashState.collect { splashState = it }
        }

        binding.tryAgain.setOnClickListener {
            binding.tryAgain.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            splashViewModel.onEvent(SplashUiEvent.TryAgain)
        }

        lifecycleScope.launch {
            splashViewModel.appDataResultChannel.collect { result ->
                when (result) {
                    is AppDataResult.Error -> {
                        Toast.makeText(
                            this@SplashActivity,
                            getString(R.string.error_connect_to_a_network_and_try_again),
                            Toast.LENGTH_LONG
                        ).show()
                        binding.tryAgain.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }

                    is AppDataResult.Loading -> {}

                    is AppDataResult.Success -> {

                        InterManager.loadInterstitial(this@SplashActivity)

                        startActivity(
                            if (!prefs.getBoolean("tipsShown", false)) {
                                Intent(this@SplashActivity, TipsActivity::class.java)
                            } else {
                                Intent(this@SplashActivity, HomeActivity::class.java)
                            }
                        )
                        finish()
                    }
                }
            }
        }


    }


}






















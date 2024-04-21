package com.ardrawing.sketchtrace.core.presentation.onboarding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.presentation.get_started.GetStartedActivity
import com.ardrawing.sketchtrace.databinding.ActivityTipsBinding
import com.ardrawing.sketchtrace.util.AppAnimation
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private var onboardingState: OnboardingState? = null

    private lateinit var binding: ActivityTipsBinding

    private var isFromSplash = true

    @Inject
    lateinit var interManager: InterManager

    @Inject
    lateinit var nativeManager: NativeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        isFromSplash = intent?.extras?.getBoolean("from_splash") ?: true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onboardingViewModel.tipsState.collect {
                    onboardingState = it
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onboardingViewModel.appData.collect { appData ->
                    nativeManager.loadNative(
                        appData,
                        findViewById(R.id.native_frame),
                        findViewById(R.id.native_temp),
                        this@OnboardingActivity, false
                    )
                }
            }
        }

        changeTip()
        binding.nextStart.setOnClickListener {
            onboardingViewModel.onEvent(OnboardingUiEvent.NextTip)
            changeTip()
        }

        binding.back.setOnClickListener {
            onboardingViewModel.onEvent(OnboardingUiEvent.Back)
            changeTip()
        }

    }

    private fun changeTip() {

        changeDotsColor()
        when (onboardingState?.tipNum) {
            1 -> {
                binding.tipTitle.text = getString(R.string.tip_title_1)
                binding.tipDesc.text = getString(R.string.tip_desc_1)
                binding.tipImage.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.tip_image_1)
                )
                binding.nextStart.text = getString(R.string.next)

                binding.back.visibility = View.GONE
            }

            2 -> {
                AppAnimation().startLeftwardScaleAnimation(binding.tipParent)

                binding.tipTitle.text = getString(R.string.tip_title_2)
                binding.tipDesc.text = getString(R.string.tip_desc_2)
                binding.tipImage.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.tip_image_2)
                )
                binding.nextStart.text = getString(R.string.next)
                binding.back.visibility = View.VISIBLE
            }

            3 -> {
                AppAnimation().startLeftwardScaleAnimation(binding.tipParent)

                binding.tipTitle.text = getString(R.string.tip_title_3)
                binding.tipDesc.text = getString(R.string.tip_desc_3)
                binding.tipImage.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.tip_image_3)
                )
                binding.nextStart.text = getString(R.string.next)
                binding.back.visibility = View.VISIBLE
            }

            4 -> {
                AppAnimation().startLeftwardScaleAnimation(binding.tipParent)

                binding.tipTitle.text = getString(R.string.tip_title_4)
                binding.tipDesc.text = getString(R.string.tip_desc_4)
                binding.tipImage.setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.tip_image_4)
                )

                binding.nextStart.setTextColor(getColor(R.color.primary_dark))
                binding.nextStart.text =
                    if (isFromSplash) getString(R.string.start) else getString(R.string.exit)
                binding.back.visibility = View.VISIBLE
            }

            5 -> {
                interManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
                    override fun onAdClosed() {
                        if (isFromSplash) {
                            onboardingViewModel.onEvent(OnboardingUiEvent.Navigate)
                            Intent(
                                this@OnboardingActivity, GetStartedActivity::class.java
                            ).also {
                                startActivity(it)
                            }
                        }
                        finish()
                    }
                })
            }
        }
    }

    private fun changeDotsColor() {

        findViewById<CardView>(R.id.dot_1).setCardBackgroundColor(getColor(R.color.primary_2))
        findViewById<CardView>(R.id.dot_2).setCardBackgroundColor(getColor(R.color.primary_2))
        findViewById<CardView>(R.id.dot_3).setCardBackgroundColor(getColor(R.color.primary_2))
        findViewById<CardView>(R.id.dot_4).setCardBackgroundColor(getColor(R.color.primary_2))

        when (onboardingState?.tipNum) {
            1 -> {
                findViewById<CardView>(R.id.dot_1).setCardBackgroundColor(getColor(R.color.primary_3))
            }

            2 -> {
                findViewById<CardView>(R.id.dot_2).setCardBackgroundColor(getColor(R.color.primary_3))
            }

            3 -> {
                findViewById<CardView>(R.id.dot_3).setCardBackgroundColor(getColor(R.color.primary_3))
            }

            4, 5 -> {
                findViewById<CardView>(R.id.dot_4).setCardBackgroundColor(getColor(R.color.primary_3))
            }
        }
    }
}
















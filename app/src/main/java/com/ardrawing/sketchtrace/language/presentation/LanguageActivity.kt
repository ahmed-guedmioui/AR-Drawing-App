package com.ardrawing.sketchtrace.language.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.util.ads_original.InterstitialAdManager
import com.ardrawing.sketchtrace.core.data.util.ads_original.NativeAdsManager
import com.ardrawing.sketchtrace.databinding.ActivityLanguageBinding
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.ardrawing.sketchtrace.onboarding.presentation.OnboardingActivity
import com.ardrawing.sketchtrace.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class LanguageActivity : AppCompatActivity() {

    val en = "en"
    val de = "de"
    val es = "es"
    val fr = "fr"
    val ja = "ja"
    val ko = "ko"
    val zh_rCN = "zh"

    private val languageViewModel: LanguageViewModel by viewModels()
    private var languageState: LanguageState? = null

    private lateinit var binding: ActivityLanguageBinding

//    @Inject
//    lateinit var interstitialManger: InterstitialManger
//
//    @Inject
//    lateinit var nativeManager: NativeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                languageViewModel.languageState.collect {
                    languageState = it
                    changeLanguage()
                }
            }
        }

       NativeAdsManager.loadNative(
           findViewById(R.id.native_frame),
           findViewById(R.id.native_temp),
           this,
           isButtonTop = false
       )

//        nativeManager.setActivity(this)
//        nativeManager.loadNative(
//            findViewById(R.id.native_frame),
//            findViewById(R.id.native_temp),
//            isButtonTop = false
//        )

        val fromSplash = intent?.extras?.getBoolean("from_splash")

        if (fromSplash == false) {
            binding.back.visibility = View.VISIBLE
            binding.back.setOnClickListener {
                finish()
            }
        }

        binding.apply.setOnClickListener {
            InterstitialAdManager.showInterstitial(
                this,
                object : InterstitialAdManager.OnAdClosedListener {
                    override fun onAdClosed() {
                        languageViewModel.onEvent(LanguageUiEvent.Navigate)

                        if (fromSplash == true) {
                            Intent(
                                this@LanguageActivity, OnboardingActivity::class.java
                            ).also {
                                startActivity(it)
                            }
                        } else {
                            Constants.languageChanged1 = true
                            Constants.languageChanged2 = true
                        }

                        finish()
                    }
                }
            )

//            interstitialManger.showInterstitial(
//                this,
//                object : InterstitialManger.OnAdClosedListener {
//                    override fun onAdClosed() {
//                        languageViewModel.onEvent(LanguageUiEvent.Navigate)
//
//                        if (fromSplash == true) {
//                            Intent(
//                                this@LanguageActivity, OnboardingActivity::class.java
//                            ).also {
//                                startActivity(it)
//                            }
//                        } else {
//                            Constants.languageChanged1 = true
//                            Constants.languageChanged2 = true
//                        }
//
//                        finish()
//                    }
//                }
//            )
        }

        binding.english.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(en))
        }

        binding.germany.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(de))
        }

        binding.chinese.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(zh_rCN))
        }

        binding.korean.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(ko))
        }

        binding.spanish.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(es))
        }

        binding.french.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(fr))
        }

        binding.japanese.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(ja))
        }

    }

    private fun changeLanguage() {

        binding.english.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.germany.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.chinese.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.korean.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.spanish.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.french.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.japanese.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)

        when (languageState?.language) {
            en -> {
                binding.english.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            de -> {
                binding.germany.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            zh_rCN -> {
                binding.chinese.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            ko -> {
                binding.korean.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            es -> {
                binding.spanish.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            fr -> {
                binding.french.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            ja -> {
                binding.japanese.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }
        }
    }

}
















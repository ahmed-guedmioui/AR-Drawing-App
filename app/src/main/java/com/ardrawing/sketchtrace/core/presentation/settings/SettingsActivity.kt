package com.ardrawing.sketchtrace.core.presentation.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.databinding.ActivitySettingsBinding
import com.ardrawing.sketchtrace.core.presentation.follow.FollowActivity
import com.ardrawing.sketchtrace.core.presentation.language.LanguageActivity
import com.ardrawing.sketchtrace.core.presentation.settings.adapter.RecommendedAppsAdapter
import com.ardrawing.sketchtrace.core.presentation.onboarding.OnboardingActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.Constants
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.openDeveloper
import com.ardrawing.sketchtrace.util.rateApp
import com.ardrawing.sketchtrace.util.shareApp
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private var settingsState: SettingsState? = null
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var consentInformation: ConsentInformation

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.settingsState.collect {
                    settingsState = it
                    settingsState?.appData?.let { appData ->
                        privacyDialog(appData)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.appData.collect { appData ->
                    appData?.let {
                        if (it.showRecommendedApps) {
                            binding.recommendedAppsRecyclerView.layoutManager =
                                LinearLayoutManager(
                                    this@SettingsActivity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )

                            binding.recommendedAppsRecyclerView.adapter =
                                RecommendedAppsAdapter(
                                    activity = this@SettingsActivity,
                                    appData = it
                                )
                        } else {
                            binding.recommendedAppsParent.visibility = View.GONE
                        }
                    }

                    if (appData?.isSubscribed == true) {
                        binding.subscribeInfo.text = getString(
                            R.string.your_subscription_will_expire_in_n,
                            settingsState?.appData?.subscriptionExpireDate
                        )
                    } else {
                        binding.subscribeInfo.text = getString(R.string.your_are_not_subscribed)
                    }
                }
            }
        }


        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.rateUs.setOnClickListener {
            rateApp(this)
        }

        binding.moreApps.setOnClickListener {
            openDeveloper(this)
        }

        binding.share.setOnClickListener {
            shareApp(this)
        }

        binding.followUs.setOnClickListener {
            startActivity(Intent(this, FollowActivity::class.java))
        }

        binding.tips.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }

        binding.privacy.setOnClickListener {
            settingsViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        binding.language.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }

        binding.subscribe.setOnClickListener {
            if (settingsState?.appData?.isSubscribed == true) {
                Toast.makeText(
                    this, getString(R.string.you_are_already_subscribed), Toast.LENGTH_SHORT
                ).show()
            } else {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
            }
        }

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        // Show a privacy options button if required.
        val isPrivacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

        if (isPrivacyOptionsRequired) {
            binding.adsOptions.visibility = View.VISIBLE
        }

        binding.adsOptions.setOnClickListener {
            getConsent()
        }

    }

    private fun getConsent() {

        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("EC63707298751E23CCEB09A07FCB3B1F")
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .build()

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@SettingsActivity
                ) { loadAndShowError ->
                    // Consent gathering failed.
                    if (loadAndShowError != null) {
                        Log.d("tag_admob", loadAndShowError.message)
                    }
                    // Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        Log.d("tag_admob", "canRequestAds 2")
                        prefs.edit().putBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, true).apply()

                    } else {
                        Log.d("tag_admob", "navigate 1")
                        prefs.edit().putBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, false).apply()
                    }
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                Log.d("tag_admob", requestConsentError.message)
                Log.d("tag_admob", "navigate 2")
                prefs.edit().putBoolean(AdsConstants.CAN_SHOW_ADMOB_ADS, false).apply()
            }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun privacyDialog(appData: AppData) {
        val privacyDialog = Dialog(this)
        privacyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        privacyDialog.setCancelable(true)
        privacyDialog.setContentView(R.layout.dialog_privacy)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(privacyDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        privacyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        privacyDialog.window!!.attributes = layoutParams

        val webView = privacyDialog.findViewById<WebView>(R.id.web_view)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(appData.privacyLink)
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl(appData.privacyLink)

        privacyDialog.setOnDismissListener {
            settingsViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (settingsState?.showPrivacyDialog == true) {
            privacyDialog.show()
        } else {
            privacyDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Constants.languageChanged1) {
            recreate()
            Constants.languageChanged1 = false
        }

        settingsViewModel.onEvent(SettingsUiEvent.UpdateAppData)

        if (settingsState?.appData?.isSubscribed == true) {
            binding.subscribeInfo.text = getString(
                R.string.your_subscription_will_expire_in_n,
                settingsState?.appData?.subscriptionExpireDate
            )
        } else {
            binding.subscribeInfo.text = getString(R.string.your_are_not_subscribed)
        }
    }

}






















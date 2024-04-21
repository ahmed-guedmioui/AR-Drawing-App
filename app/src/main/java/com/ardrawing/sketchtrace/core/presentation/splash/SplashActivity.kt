package com.ardrawing.sketchtrace.core.presentation.splash

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.presentation.get_started.GetStartedActivity
import com.ardrawing.sketchtrace.core.presentation.home.HomeActivity
import com.ardrawing.sketchtrace.core.presentation.language.LanguageActivity
import com.ardrawing.sketchtrace.core.presentation.onboarding.OnboardingActivity
import com.ardrawing.sketchtrace.databinding.ActivitySplashBinding
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.AdsConstants
import com.ardrawing.sketchtrace.util.AppAnimation
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.UrlOpener
import com.ardrawing.sketchtrace.util.ads.AdmobAppOpenManager
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    private lateinit var splashState: SplashState
    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var admobAppOpenManager: AdmobAppOpenManager

    @Inject
    lateinit var interManager: InterManager

    private var isNotificationDialogCalled = AtomicBoolean(false)
    private var canShowAds = AtomicBoolean(false)
    private lateinit var consentInformation: ConsentInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        consentInformation = UserMessagingPlatform.getConsentInformation(this)

        AppAnimation().startRepeatingAnimation(binding.animationImage)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.splashState.collect {
                    splashState = it
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.updateDialogState.collect { state ->
                    if (state > 0) {
                        updateDialog(state)
                    }
                }
            }
        }


//        admobAppOpenManager = AdmobAppOpenManager(
//            this@SplashActivity.application
//        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.continueAppChannel.collect { continueApp ->
                    if (continueApp) {
                        getConsent()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.showErrorToastChannel.collect { show ->
                    if (show) {
                        Toast.makeText(
                            this@SplashActivity,
                            getString(R.string.error_connect_to_a_network_and_try_again),
                            Toast.LENGTH_SHORT
                        ).show()

                        tryAgainButtonVisibility(true)
                    }
                }
            }
        }

        binding.tryAgain.setOnClickListener {
            tryAgainButtonVisibility(false)
            splashViewModel.onEvent(SplashUiEvent.TryAgain)
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
                    this@SplashActivity
                ) { loadAndShowError ->
                    // Consent gathering failed.
                    if (loadAndShowError != null) {
                        Log.d("tag_admob", loadAndShowError.message)
                    }
                    // Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        Log.d("tag_admob", "canRequestAds 2")
                        canShowAds.set(true)
                        notificationPermissionDialog()
                    } else {
                        canShowAds.set(false)
                        Log.d("tag_admob", "navigate 1")
                        notificationPermissionDialog()
                    }
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                Log.d("tag_admob", requestConsentError.message)
                canShowAds.set(false)
                Log.d("tag_admob", "navigate 2")
                notificationPermissionDialog()
            }
        )

        if (consentInformation.canRequestAds()) {
            canShowAds.set(true)
            Log.d("tag_admob", "canRequestAds 1")
            notificationPermissionDialog()
        }
    }

    private fun notificationPermissionDialog() {
        if (isNotificationDialogCalled.getAndSet(true)) {
            return
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101
                )
            } else {
                if (canShowAds.get()) {
                    loadAds()
                } else {
                    navigate()
                }
            }
        } else {
            if (canShowAds.get()) {
                loadAds()
            } else {
                navigate()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 101) {
            if (canShowAds.get()) {
                loadAds()
            } else {
                navigate()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadAds() {

        splashViewModel.onEvent(
            SplashUiEvent.OnAdmobConsent(canShowAds.get())
        )

        MobileAds.initialize(this)
        interManager.appData = splashState.appData
        interManager.loadInterstitial(
            activity = this@SplashActivity,
        )

        admobAppOpenManager.showSplashAd(
            activity = this,
            appData = splashState.appData
        ) {
            navigate()
        }
    }

    private fun navigate() {

        tryAgainButtonVisibility(false)

        if (!splashState.isLanguageChosen) {
            Intent(this@SplashActivity, LanguageActivity::class.java).also {
                it.putExtra("from_splash", true)
                startActivity(it)
            }

        } else if (!splashState.isOnboardingShown) {
            Intent(this@SplashActivity, OnboardingActivity::class.java).also {
                startActivity(it)
            }

        } else if (!splashState.isGetStartedShown) {
            Intent(this@SplashActivity, GetStartedActivity::class.java).also {
                startActivity(it)
            }

        } else {
            checkSubscriptionBeforeGoingHome()
        }

        finish()
    }


    private fun checkSubscriptionBeforeGoingHome() {
        if (splashState.appData?.isSubscribed == true) {
            splashViewModel.onEvent(SplashUiEvent.AlreadySubscribed)
            goToHome()
        } else {
            Intent(this, PaywallActivity::class.java).also {
                it.putExtra("toHome", true)
                startActivity(it)
            }
        }
    }

    private fun goToHome() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
        }
    }


    private fun updateDialog(state: Int) {

        val isSuspended = state == 2

        val updateDialog = Dialog(this)
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        updateDialog.setCancelable(!isSuspended)
        updateDialog.setContentView(R.layout.dialog_app_update)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(updateDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateDialog.window!!.attributes = layoutParams

        updateDialog.findViewById<Button>(R.id.update).setOnClickListener {
            if (isSuspended) {
                splashState.appData?.let { appData ->
                    UrlOpener.open(this, appData.suspendedURL)
                }
            } else {
                UrlOpener.open(this, BuildConfig.APPLICATION_ID)
            }
        }

        if (!isSuspended) {
            updateDialog.findViewById<ImageView>(R.id.close).visibility = View.VISIBLE
        } else {
            splashState.appData?.let { appData ->
                updateDialog.findViewById<TextView>(R.id.title).text =
                    appData.suspendedTitle
                updateDialog.findViewById<TextView>(R.id.msg).text =
                    appData.suspendedMessage
            }
        }

        updateDialog.setOnDismissListener {
            binding.progressBar.visibility = View.VISIBLE
            splashViewModel.onEvent(SplashUiEvent.ContinueApp)
        }

        updateDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
        binding.progressBar.visibility = View.GONE

    }

    private fun tryAgainButtonVisibility(show: Boolean) {
        if (show) {
            binding.tryAgain.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        } else {
            binding.tryAgain.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }
    }
}






















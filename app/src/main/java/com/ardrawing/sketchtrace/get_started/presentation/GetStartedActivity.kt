package com.ardrawing.sketchtrace.get_started.presentation

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.util.ads_original.InterstitialAdManager
import com.ardrawing.sketchtrace.core.data.util.ads_original.NativeAdsManager
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.databinding.ActivityGetStartedBinding
import com.ardrawing.sketchtrace.home.presentation.HomeActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterstitialManger
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class GetStartedActivity : AppCompatActivity() {

    private val getStartedViewModel: GetStartedViewModel by viewModels()

    private var getStartedState: GetStartedState? = null
    private lateinit var binding: ActivityGetStartedBinding

    @Inject
    lateinit var interstitialManger: InterstitialManger

    @Inject
    lateinit var nativeManager: NativeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getStartedViewModel.getsStartedState.collect {
                    getStartedState = it

                    getStartedState?.appData?.let { appData ->
                        privacyDialog(appData)
                    }
                }
            }
        }

        binding.privacyPolicy.setOnClickListener {
            getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
        }

        binding.getStarted.setOnClickListener {
//            InterstitialAdManager.showInterstitial(
//                this,
//                object : InterstitialAdManager.OnAdClosedListener {
//                    override fun onAdClosed() {
//                        getStartedViewModel.onEvent(GetStartedUiEvent.Navigate)
//                        navigate()
//                    }
//                }
//            )

            interstitialManger.showInterstitial(
                this,
                object : InterstitialManger.OnAdClosedListener {
                    override fun onAdClosed() {
                        getStartedViewModel.onEvent(GetStartedUiEvent.Navigate)
                        navigate()
                    }
                }
            )
        }

//        NativeAdsManager.loadNative(
//            findViewById(R.id.native_frame),
//            findViewById(R.id.native_temp),
//            this,
//            isButtonTop = false
//        )

        nativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            isButtonTop = false,
            this
        )

    }

    private fun navigate() {
        if (getStartedState?.appData?.isSubscribed == true) {
            goToHome()
        } else {
            Intent(this, PaywallActivity::class.java).also {
                it.putExtra("toHome", true)
                startActivity(it)
                finish()
            }
        }
    }

    private fun goToHome() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
            finish()
        }
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
            getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (getStartedState?.showPrivacyDialog == true) {
            privacyDialog.show()
        } else {
            privacyDialog.dismiss()
        }
    }
}
















package com.med.drawing.main.presentaion.settings

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import com.med.drawing.util.LanguageChanger
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.med.drawing.App
import com.med.drawing.BuildConfig
import com.med.drawing.R
import com.med.drawing.main.presentaion.follow.FollowActivity
import com.med.drawing.main.presentaion.settings.adapter.RecommendedAppsAdapter
import com.med.drawing.databinding.ActivitySettingsBinding
import com.med.drawing.main.presentaion.language.LanguageActivity
import com.med.drawing.splash.data.DataManager
import com.med.drawing.util.Constants
import com.med.drawing.util.openDeveloper
import com.med.drawing.util.rateApp
import com.med.drawing.util.shareApp
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val splashViewModel: SettingsViewModel by viewModels()

    private lateinit var settingsState: SettingsState
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            splashViewModel.settingsState.collect {
                settingsState = it
                privacyDialog()
            }
        }

        if (DataManager.appData.showRecommendedApps) {
            binding.recommendedAppsRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            binding.recommendedAppsRecyclerView.adapter = RecommendedAppsAdapter(this)
        } else {
            binding.recommendedAppsParent.visibility = View.GONE
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

        binding.privacy.setOnClickListener {
            splashViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        binding.language.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }


        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.d("REVENUE_CUT", "getOfferingsWith: onError: ${error.underlyingErrorMessage}")
            },
            onSuccess = { offerings ->
                Log.d("REVENUE_CUT", "getOfferingsWith: onSuccess: ${offerings.current}")
            }
        )

//        Purchases.sharedInstance.purchaseWith(
//            PurchaseParams.Builder(
//                this, xxx
//            ).build(),
//            onError = { error, userCancelled -> /* No purchase */ },
//            onSuccess = { storeTransaction, customerInfo ->
//                if (customerInfo.entitlements["j"]?.isActive == true) {
//                    // Unlock that great "pro" content
//                }
//
//            }
//        )
    }

    private fun privacyDialog() {
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

        privacyDialog.findViewById<TextView>(R.id.privacy_policy).text =
            getString(R.string.app_privacy_policy)

        privacyDialog.setOnDismissListener {
            splashViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (settingsState.showPrivacyDialog) {
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
    }
}






















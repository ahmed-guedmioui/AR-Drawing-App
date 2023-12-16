package com.med.drawing.main.presentaion.get_started

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.App
import com.med.drawing.R
import com.med.drawing.util.ads.NativeManager
import com.med.drawing.main.presentaion.home.HomeActivity
import com.med.drawing.databinding.ActivityGetStartedBinding
import com.med.drawing.util.ads.InterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class GetStartedActivity : AppCompatActivity() {

    private val getStartedViewModel: GetStartedViewModel by viewModels()

    private lateinit var getStartedState: GetStartedState
    private lateinit var binding: ActivityGetStartedBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            getStartedViewModel.getsStartedState.collect {
                getStartedState = it
                privacyDialog()
            }
        }

        privacyColor()
        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, true
        )

        binding.getStarted.setOnClickListener {
            InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
                override fun onAdClosed() {
                    prefs.edit().putBoolean("getStartedShown", true).apply()
                    startActivity(Intent(this@GetStartedActivity, HomeActivity::class.java))
                    finish()
                }
            })
        }

    }

    private fun privacyColor() {
        val s = "Privacy Policy"
        val spannableString = SpannableString(binding.privacy.text)

        val start: Int = binding.privacy.text.indexOf(s)
        val end = start + s.length


        spannableString.setSpan(
            UnderlineSpan(),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
            }
        }, start, end, 0)

        binding.privacy.text = spannableString
        binding.privacy.movementMethod = LinkMovementMethod.getInstance()
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

        privacyDialog.findViewById<TextView>(R.id.privacy_policy).text = App.PRIVACY

        privacyDialog.setOnDismissListener {
            getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (getStartedState.showPrivacyDialog) {
            privacyDialog.show()
        } else {
            privacyDialog.dismiss()
        }
    }
}















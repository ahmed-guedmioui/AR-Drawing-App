package com.ardrawing.sketchtrace.follow.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ardrawing.sketchtrace.App.Companion.facebook
import com.ardrawing.sketchtrace.App.Companion.instagram
import com.ardrawing.sketchtrace.App.Companion.tiktok
import com.ardrawing.sketchtrace.App.Companion.x
import com.ardrawing.sketchtrace.databinding.ActivityFollowBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityFollowBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.tiktok.setOnClickListener {
            openAppWithUserId("tiktok")
        }

        binding.facebook.setOnClickListener {
            openAppWithUserId("facebook")
        }

        binding.instagram.setOnClickListener {
            openAppWithUserId("instagram")
        }

        binding.x.setOnClickListener {
            openAppWithUserId("x")
        }

        if (tiktok.isEmpty()) binding.tiktok.visibility = View.GONE
        if (facebook.isEmpty()) binding.facebook.visibility = View.GONE
        if (instagram.isEmpty()) binding.instagram.visibility = View.GONE
        if (x.isEmpty()) binding.x.visibility = View.GONE


    }

    private fun openAppWithUserId(name: String) {
        val appIntent = Intent(Intent.ACTION_VIEW)

        when (name) {
            "tiktok" -> { // TikTok
                appIntent.data = Uri.parse("https://www.tiktok.com/@$tiktok")
            }

            "facebook" -> { // Facebook
                appIntent.data = Uri.parse("https://www.facebook.com/$facebook")
            }

            "instagram" -> { // Instagram
                appIntent.data = Uri.parse("https://www.instagram.com/$instagram")
            }

            "x" -> { // Twitter
                appIntent.data = Uri.parse("https://twitter.com/$x")
            }
        }

        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }


}





















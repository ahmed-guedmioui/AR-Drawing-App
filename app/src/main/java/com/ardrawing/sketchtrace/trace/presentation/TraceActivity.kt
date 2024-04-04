package com.ardrawing.sketchtrace.trace.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.databinding.ActivityTraceBinding
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import com.ardrawing.sketchtrace.util.other.MultiTouch
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.thebluealliance.spectrum.SpectrumDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("ClickableViewAccessibility")

@AndroidEntryPoint
class TraceActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var appDataRepository: AppDataRepository

    @Inject
    lateinit var imageCategoriesRepository: ImageCategoriesRepository

    private lateinit var binding: ActivityTraceBinding

    private var bmOriginal: Bitmap? = null
    private var brightness: Int = 0
    private var isLock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityTraceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (appDataRepository.getAppData()?.isSubscribed == true) {
            binding.vipPhoto.visibility = View.GONE
            binding.vipVideo.visibility = View.GONE
        }

       val pushAnim = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.apply {

            binding.relEditRound.setOnClickListener {
                it.startAnimation(pushAnim)
                colorDialog()
            }

            relFlip.setOnClickListener { flip ->
                flip.startAnimation(pushAnim)
                bmOriginal = flip(bmOriginal)
                bmOriginal?.let {
                    objImage.setImageBitmap(it)
                }
            }

            relCamera.setOnClickListener {
                it.startAnimation(pushAnim)
                rewarded {
                    getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                        ImagePicker.with(this@TraceActivity)
                            .cameraOnly()
                            .saveDir(it1)
                            .createIntent { intent ->
                                startForGetPhotoResult.launch(intent)
                            }
                    }
                }

            }

            relGallery.setOnClickListener {
                it.startAnimation(pushAnim)
                rewarded {
                    ImagePicker.with(this@TraceActivity)
                        .galleryOnly()
                        .createIntent { intent ->
                            startForGetPhotoResult.launch(intent)
                        }
                }
            }

            relLock.setOnClickListener {
                it.startAnimation(pushAnim)
                if (!isLock) {
                    objImage.isEnabled = false
                    isLock = true
                    icLock.setImageResource(R.drawable.unlock)
                } else {
                    objImage.isEnabled = true
                    isLock = false
                    icLock.setImageResource(R.drawable.lock)
                }
            }

            alphaSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    objImage.alpha = (alphaSeek.max - progress) / 10.0f
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val i = Resources.getSystem().displayMetrics.widthPixels
        val imagePath = intent.extras?.getString("imagePath")


        val window = window
        binding.brightnessSeek.max = 255
        binding.brightnessSeek.keyProgressIncrement = 1
        try {
            brightness = Settings.System.getInt(
                contentResolver, getString(R.string.screen_brightness)
            )
            binding.brightnessSeek.progress = brightness / 255
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        binding.brightnessSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int, fromUser: Boolean
            ) {
                brightness = progress

                val attributes = window.attributes
                attributes.screenBrightness = brightness / 255.0f
                window.attributes = attributes

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        if (imagePath != null) {
            Glide.with(this)
                .asBitmap()
                .load(imagePath)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bmOriginal = resource
                        val imageView = binding.objImage
                        val d = i.toDouble()
                        imageView.setOnTouchListener(
                            MultiTouch(
                                imageView, 1.0f, 1.0f,
                                (d / 3.5).toInt().toFloat(), 600.0f
                            )
                        )
                        val bitmap = bmOriginal
                        if (bitmap != null) {
                            binding.objImage.setImageBitmap(bitmap)
                        } else {
                            Toast.makeText(
                                this@TraceActivity,
                                getString(R.string.some_issue_with_this_image_try_another_one),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        binding.animationView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animationView.visibility = View.GONE
        }, 7000L)

    }

    private fun rewarded(onRewDone: () -> Unit) {
        RewardedManager.appData = appDataRepository.getAppData()
        RewardedManager.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManager.OnAdClosedListener {
                override fun onRewClosed() {
                    onRewDone()
                }

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@TraceActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRewComplete() {
                }
            },
            isImages = false,
            onOpenPaywall = {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
            }
        )
    }

    private fun colorDialog() {
        SpectrumDialog.Builder(this)
            .setColors(R.array.demo_colors)
            .setSelectedColorRes(R.color.black)
            .setDismissOnColorSelected(true)
            .setOutlineWidth(2)
            .setFixedColumnCount(4)
            .setOnColorSelectedListener { _, i ->
                binding.mainLayout.setBackgroundColor(i)
            }
            .build()
            .show(supportFragmentManager, getString(R.string.color))
    }

    private fun flip(bitmap: Bitmap?): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        return try {
            Bitmap.createBitmap(
                bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
        } catch (e: Exception) {
            null
        }
    }

    private val startForGetPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri = result.data?.data!!

                val i = Resources.getSystem().displayMetrics.widthPixels
                Glide.with(this)
                    .asBitmap()
                    .load(uri.toString())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bmOriginal = resource
                            val imageView = binding.objImage
                            val d = i.toDouble()
                            imageView.setOnTouchListener(
                                MultiTouch(
                                    imageView, 1.0f, 1.0f, (d / 3.5).toInt().toFloat(), 600.0f
                                )
                            )
                            val bitmap = bmOriginal
                            if (bitmap != null) {
                                binding.objImage.setImageBitmap(bitmap)
                            } else {
                                Toast.makeText(
                                    this@TraceActivity,
                                    getString(R.string.some_issue_with_this_image_try_another_one),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                Toast.makeText(
                    this, getString(R.string.error_importing_photo), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE_CAMERA && (grantResults.isEmpty() || grantResults[0] != 0)) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            finish()
        }
        if (requestCode != PERMISSION_CODE_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        if (appDataRepository.getAppData()?.isSubscribed == true) {
            binding.vipPhoto.visibility = View.GONE
            binding.vipVideo.visibility = View.GONE
        }
    }

    companion object {
        private val PERMISSION_CODE_CAMERA = 3002
    }
}

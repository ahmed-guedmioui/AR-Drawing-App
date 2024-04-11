package com.ardrawing.sketchtrace.trace.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityTraceBinding
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.thebluealliance.spectrum.SpectrumDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("ClickableViewAccessibility")
@AndroidEntryPoint
class TraceActivity : AppCompatActivity() {

    private val traceViewModel: TraceViewModel by viewModels()
    private var traceState: TraceState? = null

    private lateinit var binding: ActivityTraceBinding

    private var bmOriginal: Bitmap? = null

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityTraceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                traceViewModel.traceState.collect {
                    traceState = it

                    if (traceState?.isSubscribed == true) {
                        binding.vipPhoto.visibility = View.GONE
                        binding.vipVideo.visibility = View.GONE
                    }

                    if (traceState?.isActivityInitialized == false) {
                        traceViewModel.onEvent(TraceUiEvent.InitializedActivity)
                        showStartAnimation()
                    }

                    binding.mainLayout.setBackgroundColor(
                        traceState?.backgroundColor ?: Color.TRANSPARENT
                    )

//                    setImageEnabled()
                }
            }
        }

        val imagePath = intent.extras?.getString("imagePath")
        imagePath?.let {
            loadImage(it)
        }
        actions()

    }

    private fun showStartAnimation() {
        binding.animationView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animationView.visibility = View.GONE
        }, 4000)
    }

    private fun loadImage(path: String) {
        Glide.with(this)
            .asBitmap()
            .load(path)
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        bmOriginal = resource
                        bmOriginal?.let {
                            if (traceState?.isFlipped == true) {
                                flip(it)
                            }
                            binding.objImage.setImageBitmap(it)
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Toast.makeText(
                            this@TraceActivity,
                            getString(R.string.some_issue_with_this_image_try_another_one),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                }
            )
    }

    private fun actions() {
        val window = window
        val pushAnim = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.apply {
            relEditRound.setOnClickListener {
                it.startAnimation(pushAnim)
                colorDialog()
            }

            relFlip.setOnClickListener { flip ->
                flip.startAnimation(pushAnim)
                bmOriginal?.let {
                    bmOriginal = flip(it)
                    traceViewModel.onEvent(TraceUiEvent.UpdateIsFlipped)
                }

                bmOriginal?.let {
                    objImage.setImageBitmap(it)
                }
            }

            relCamera.setOnClickListener {
                it.startAnimation(pushAnim)
                rewarded {
                    getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                        ImagePicker.with(this@TraceActivity).cameraOnly().saveDir(it1)
                            .createIntent { intent ->
                                startForGetPhotoResult.launch(intent)
                            }
                    }
                }

            }

            relGallery.setOnClickListener {
                it.startAnimation(pushAnim)
                rewarded {
                    ImagePicker.with(this@TraceActivity).galleryOnly().createIntent { intent ->
                        startForGetPhotoResult.launch(intent)
                    }
                }
            }

            relLock.setOnClickListener {
                it.startAnimation(pushAnim)
                traceViewModel.onEvent(TraceUiEvent.UpdateIsEnabled)
            }

            alphaSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    val transparency = (alphaSeek.max - progress) / 10f
                    traceViewModel.onEvent(
                        TraceUiEvent.UpdateTransparency(transparency)
                    )
                    objImage.alpha = transparency
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            binding.brightnessSeek.keyProgressIncrement = 1
            brightnessSeek.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar, progress: Int, fromUser: Boolean
                    ) {
                        val brightness = progress / 100f

                        traceViewModel.onEvent(
                            TraceUiEvent.UpdateBrightness(brightness)
                        )

                        val attributes = window.attributes
                        attributes.screenBrightness = brightness
                        window.attributes = attributes
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                }
            )
        }
    }

    private fun setImageEnabled() {
        traceState?.let {
            binding.apply {
                if (it.isEnabled) {
                    objImage.isEnabled = true
                    icLock.setImageResource(R.drawable.lock)
                    Log.d("tag_trace", "Enabled = true")
                } else {
                    objImage.isEnabled = false
                    icLock.setImageResource(R.drawable.unlock)
                    Log.d("tag_trace", "Enabled = false")
                }
            }
        }
    }

    private fun rewarded(onRewDone: () -> Unit) {
        RewardedManager.appData = traceState?.appData
        RewardedManager.showRewarded(activity = this,
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
            })
    }

    private fun colorDialog() {
        val colorDialog = SpectrumDialog.Builder(this)
            .setColors(R.array.demo_colors)
            .setSelectedColorRes(R.color.transparent)
            .setDismissOnColorSelected(true)
            .setOutlineWidth(2)
            .setFixedColumnCount(4)
            .setOnColorSelectedListener { _, color ->
                traceViewModel.onEvent(
                    TraceUiEvent.UpdateBackgroundColor(color)
                )
                binding.mainLayout.setBackgroundColor(color)
            }
            .build()
            .show(supportFragmentManager, getString(R.string.color))
    }

    private fun flip(bitmap: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        return try {
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
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
                val uri = result.data?.data
                loadImage(uri.toString())
            } else {
                Toast.makeText(
                    this, getString(R.string.error_importing_photo), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
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

        if (traceState?.isSubscribed == true) {
            binding.vipPhoto.visibility = View.GONE
            binding.vipVideo.visibility = View.GONE
        }
    }

    companion object {
        private const val PERMISSION_CODE_CAMERA = 3002
    }
}

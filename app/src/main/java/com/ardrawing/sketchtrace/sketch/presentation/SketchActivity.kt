package com.ardrawing.sketchtrace.sketch.presentation

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.util.PermissionUtils
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeManager
import com.ardrawing.sketchtrace.core.domain.repository.ads.RewardedManger
import com.ardrawing.sketchtrace.creation.domian.repository.CreationRepository
import com.ardrawing.sketchtrace.databinding.ActivitySketchBinding
import com.ardrawing.sketchtrace.image_editor.presentation.ImageEditorActivity
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.sketch.presentation.util.SketchBitmap
import com.ardrawing.sketchtrace.util.other_util.MultiTouch
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.play.core.review.ReviewManagerFactory
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class SketchActivity : AppCompatActivity() {

    @Inject
    lateinit var creationRepository: CreationRepository

    @Inject
    lateinit var rewardedManger: RewardedManger

    @Inject
    lateinit var nativeManager: NativeManager

    private lateinit var binding: ActivitySketchBinding

    private val sketchViewModel: SketchViewModel by viewModels()

    private var sketchState: SketchState? = null
    private var appDataState: AppData? = null
    private var isActivityInitializedState: Boolean = false
    private var imageBorderState: Boolean = false

    private var isTakePhotoDialogShowingState = false
    private var isSavePhotoDialogShowingState = false

    private var isTimeFinishedDialogShowingState = false

    private var videoElapsedTimeMillis: Long = 0
    private var isRecording = false
    private var isSavedVideoCalled = true
    private var videoHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivitySketchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent?.extras?.getString("imagePath")

        collectState(sketchViewModel.appData) {
            appDataState = it

            if (!isActivityInitializedState) {
                sketchViewModel.onEvent(SketchUiEvent.InitializeActivity())
            }
        }

        collectState(sketchViewModel.isActivityInitializedState) {
            isActivityInitializedState = it

            if (!isActivityInitializedState) {
                sketchViewModel.onEvent(
                    SketchUiEvent.InitializeActivity(true)
                )
                initializeActivity(imagePath)
            }
        }

        collectState(sketchViewModel.sketchState) {
            sketchState = it
            setImageLock()
            binding.objImage.alpha = sketchState?.imageTransparency ?: 50f
        }

        collectState(sketchViewModel.imageBorderState) {
            imageBorderState = it
            setImageBorder(imageBorderState)
        }

        collectState(sketchViewModel.flashState, ::switchFlash)

        timeFinishedDialog()
        collectState(sketchViewModel.isTimeFinishedDialogShowingState) {
            isTimeFinishedDialogShowingState = it

            if (isTimeFinishedDialogShowingState) {
                timeFinishedDialog.show()
            } else {
                timeFinishedDialog.dismiss()
            }
        }

        takePhotoDialog()
        collectState(sketchViewModel.isTakePhotoDialogShowingState) {
            isTakePhotoDialogShowingState = it

            if (isTakePhotoDialogShowingState) {
                takePhotoDialog.show()
            } else {
                takePhotoDialog.dismiss()
            }
        }

        savePhotoDialog()
        collectState(sketchViewModel.isSavePhotoDialogShowingState) {
            isSavePhotoDialogShowingState = it

            if (isSavePhotoDialogShowingState) {
                savePhotoDialog.findViewById<ImageView>(R.id.photo)
                    .setImageBitmap(SketchBitmap.bitmapToSave)

                savePhotoDialog.show()
            } else {
                savePhotoDialog.dismiss()
            }
        }

        collectState(sketchViewModel.countdownTimeState) { remainingTime ->

            binding.mainTemp.text = remainingTime

            if (remainingTime == "04:00") {
                binding.theDrawingIsReadyBtn.visibility = View.VISIBLE
            }

            if (remainingTime == "00:00") {
                if (appDataState?.isSubscribed == false) {
                    sketchViewModel.onEvent(
                        SketchUiEvent.ShowAndHideTimeFinishedDialog(true)
                    )
                }
            }
        }

        videoHandler = Handler(Looper.getMainLooper())
        val pushAnime = AnimationUtils.loadAnimation(this, R.anim.view_push)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val isFlashSupported = cameraManager.getCameraCharacteristics("0")
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

        if (!isFlashSupported) {
            binding.relFlash.visibility = View.GONE
        }

        nativeManager.setActivity(this)
        nativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            isButtonTop = true
        )

        binding.objImage.setOnTouchListener(
            MultiTouch(binding.objImage)
        )

        binding.apply {

            close.setOnClickListener {
                binding.nativeParent.visibility = View.GONE
            }

            theDrawingIsReadyBtn.setOnClickListener {
                sketchViewModel.onEvent(
                    SketchUiEvent.ShowAndHideTakePhotoDialog(true)
                )
            }

            relCamera.setOnClickListener {
                it.startAnimation(pushAnime)
                rewarded {
                    getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                        ImagePicker.with(this@SketchActivity)
                            .cameraOnly()
                            .saveDir(it1)
                            .createIntent { intent ->
                                startForGetPhotoResult.launch(intent)
                            }
                    }
                }
            }

            relGallery.setOnClickListener {
                it.startAnimation(pushAnime)
                rewarded {
                    ImagePicker.with(this@SketchActivity)
                        .galleryOnly()
                        .createIntent { intent ->
                            startForGetPhotoResult.launch(intent)
                        }
                }
            }

            relFlip.setOnClickListener {
                it.startAnimation(pushAnime)
                flipImage()
            }

            relEditRound.setOnClickListener {
                sketchViewModel.onEvent(
                    SketchUiEvent.UpdateIsImageBordered
                )
            }

            binding.advanced.setOnClickListener {
                Intent(
                    this@SketchActivity, ImageEditorActivity::class.java
                ).also(::startActivity)
            }

            alphaSeek.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar) {}

                    override fun onProgressChanged(
                        seekBar: SeekBar, progress: Int, fromUser: Boolean
                    ) {
                        val transparency = (alphaSeek.max - progress) / 10f
                        sketchViewModel.onEvent(
                            SketchUiEvent.UpdateImageTransparency(transparency)
                        )
                        objImage.alpha = transparency

                        objImage.alpha = (alphaSeek.max - progress) / 10.0f
                    }
                }
            )

            relFlash.setOnClickListener {
                sketchViewModel.onEvent(SketchUiEvent.UpdateIsFlashEnabled)
            }

            relLock.setOnClickListener {
                sketchViewModel.onEvent(SketchUiEvent.UpdateIsImageLocked)
            }
        }
    }

    private fun <T> LifecycleOwner.collectState(
        stateFlow: StateFlow<T>,
        collect: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect { value ->
                    collect(value)
                }
            }
        }
    }

    private fun initializeActivity(imagePath: String?) {

        if (appDataState?.isSubscribed == true) {
            binding.mainTempContainer.visibility = View.GONE
            binding.vipPhoto.visibility = View.GONE
            binding.vipVideo.visibility = View.GONE
            binding.vipRecord.visibility = View.GONE
        }

        imagePath?.let(::loadImage)

        showStartAnimation()

        if (appDataState?.isSubscribed == false) {
            binding.theDrawingIsReadyBtn.visibility = View.GONE
            sketchViewModel.onEvent(
                SketchUiEvent.StartAndStopCountdownTimer(true)
            )
        }
    }

    private fun loadImage(imagePath: String) {
        Glide.with(this)
            .asBitmap()
            .load(imagePath)
            .into(
                object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        SketchBitmap.bitmap = resource
                        binding.objImage.apply {
                            setImageBitmap(SketchBitmap.bitmap)
                            binding.imgOutline.setImageResource(R.drawable.outline)
                            alpha = 0.6f
                            binding.alphaSeek.progress = 4
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                }
            )
    }

    private fun showStartAnimation() {
        binding.animationView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animationView.visibility = View.GONE
        }, 3000)
    }

    private fun flipImage() {
        SketchBitmap.bitmap = flip(SketchBitmap.bitmap)
        SketchBitmap.borderedBitmap = flip(SketchBitmap.borderedBitmap)

        binding.objImage.setImageBitmap(
            if (!imageBorderState) SketchBitmap.bitmap
            else SketchBitmap.borderedBitmap
        )
    }

    private fun setImageLock() {
        if (sketchState?.isImageLocked == true) {
            binding.objImage.isEnabled = false
            binding.icLock.setImageResource(R.drawable.unlock)
        } else {
            binding.objImage.isEnabled = true
            binding.icLock.setImageResource(R.drawable.lock)
        }
    }

    private fun setImageBorder(isImageBordered: Boolean) {

        if (isImageBordered) {

            val progressDialog = ProgressDialog(this@SketchActivity)
            progressDialog.setCancelable(false)
            progressDialog.setMessage(getString(R.string.convert_bitmap))
            progressDialog.show()

            try {
                lifecycleScope.launch(Dispatchers.IO) {
                    val gPUImage = GPUImage(this@SketchActivity)
                    gPUImage.setImage(SketchBitmap.bitmap)
                    gPUImage.setFilter(GPUImageThresholdEdgeDetectionFilter())
                    val bitmapWithFilterApplied = gPUImage.bitmapWithFilterApplied

                    if (bitmapWithFilterApplied != null) {
                        SketchBitmap.borderedBitmap = getBitmapWithTransparentBG(
                            bitmapWithFilterApplied, -1
                        )
                    }

                    if (SketchBitmap.borderedBitmap != null) {
                        withContext(Dispatchers.Main) {
                            binding.objImage.setImageBitmap(SketchBitmap.borderedBitmap)
                            binding.imgOutline.setImageResource(R.drawable.normal)
                            progressDialog.dismiss()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SketchActivity,
                                getString(R.string.can_t_convert_this_image_try_with_another),
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                progressDialog.dismiss()
                Toast.makeText(
                    this@SketchActivity,
                    getString(R.string.can_t_convert_this_image_try_with_another),
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
            }

        } else {
            binding.objImage.setImageBitmap(SketchBitmap.bitmap)
            binding.imgOutline.setImageResource(R.drawable.outline)
        }

    }

    private fun switchFlash(isFlashEnabled: Boolean) {
        try {
            if (isFlashEnabled) {
                binding.icFlash.setImageResource(R.drawable.ic_flash_on)
                binding.cameraView.flash = Flash.TORCH
            } else {
                binding.icFlash.setImageResource(R.drawable.ic_flash_off)
                binding.cameraView.flash = Flash.OFF
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val startForGetPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri = result.data?.data!!

                Glide.with(this).asBitmap().load(uri.toString())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap, transition: Transition<in Bitmap>?
                        ) {
                            SketchBitmap.bitmap = resource
                            binding.objImage.apply {
                                setImageBitmap(SketchBitmap.bitmap)
                                binding.imgOutline.setImageResource(R.drawable.outline)
                                alpha = 0.6f
                                binding.alphaSeek.progress = 4
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

    private fun setupCameraCallbacks() {
        binding.cameraView.mode = Mode.VIDEO
        binding.recordVideo.setOnClickListener {
            if (isRecording) {
                binding.cameraView.stopVideo()
                stopVideo()
            } else {
                rewarded {
                    Handler(Looper.getMainLooper()).postDelayed({
                        isSavedVideoCalled = false
                        takeVideo()
                    }, 500)
                }
            }
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {
                if (!isSavedVideoCalled) {
                    isSavedVideoCalled = true

                    saveRecordedVideo(result.file)
                }
            }
        })
    }

    private fun saveRecordedVideo(file: File) {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.speeding_up_and_saving_video))
        progressDialog.setCancelable(false)

        if (binding.fastVideoCheck.isChecked) {
            progressDialog.show()
        }

        lifecycleScope.launch {
            var isSaved = false
            val job = launch {
                isSaved = creationRepository.insertVideoCreation(
                    file, binding.fastVideoCheck.isChecked
                )
            }

            job.join()
            Toast.makeText(
                this@SketchActivity,
                if (isSaved) application.getString(R.string.video_saved)
                else getString(R.string.something_went_wrong_while_saving_video),
                Toast.LENGTH_SHORT
            ).show()

            progressDialog.dismiss()
            binding.fastVideoCheck.isChecked = false

            creationRepository.deleteTempCreation(tempFilePath)
        }
    }

    private fun setSubscribedUser() {
        if (appDataState?.isSubscribed == true) {
            videoHandler?.removeCallbacks(videoTimerRunnable)

            binding.theDrawingIsReadyBtn.visibility = View.GONE
            sketchViewModel.onEvent(
                SketchUiEvent.StartAndStopCountdownTimer(false)
            )
//            countDownTimer?.cancel()

            binding.mainTempContainer.visibility = View.GONE
            binding.vipPhoto.visibility = View.GONE
            binding.vipVideo.visibility = View.GONE
            binding.vipRecord.visibility = View.GONE
        }
    }

    // Video ------------------
    private fun stopVideo() {
        isRecording = false
        videoHandler?.removeCallbacks(videoTimerRunnable)
        binding.recordVideoImage.setImageDrawable(
            AppCompatResources.getDrawable(
                this@SketchActivity, R.drawable.rec
            )
        )

        videoElapsedTimeMillis = 0
        binding.videoTemp.visibility = View.GONE
        binding.fastVideoCheck.visibility = View.GONE
        binding.videoTemp.text = getString(R.string._00_00)
    }

    private lateinit var tempFilePath: String
    private fun takeVideo() {
        try {

            val timestamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(Date())
            val videoFile = File(filesDir, "VIDEO_$timestamp.mp4")

            tempFilePath = Uri.fromFile(videoFile).toString()

            binding.cameraView.takeVideo(videoFile)
            isRecording = true
            binding.recordVideoImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this, R.drawable.rec_stop
                )
            )

            binding.videoTemp.visibility = View.VISIBLE
            binding.fastVideoCheck.visibility = View.VISIBLE
            videoHandler?.postDelayed(videoTimerRunnable, 1000)

        } catch (e: IOException) {
            e.printStackTrace()

            Toast.makeText(
                this, getString(R.string.error_recording_the_video), Toast.LENGTH_SHORT
            ).show()

            stopVideo()
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this, getString(R.string.error_recording_the_video), Toast.LENGTH_SHORT
            ).show()

            stopVideo()
        }

    }

    private val videoTimerRunnable = object : Runnable {
        override fun run() {
            // Update the elapsed time
            videoElapsedTimeMillis += 1000
            updateVideoTimerText()

            // Schedule the next update after 1 second
            videoHandler?.postDelayed(this, 1000)
        }
    }

    private fun updateVideoTimerText() {
        val seconds = videoElapsedTimeMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val timerText = String.format(
            null, "%02d:%02d", minutes, remainingSeconds
        )

        binding.videoTemp.text = timerText
    }
// Video ------------------

    private lateinit var timeFinishedDialog: Dialog
    private fun timeFinishedDialog() {
        timeFinishedDialog = Dialog(this)
        timeFinishedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        timeFinishedDialog.setCancelable(false)
        timeFinishedDialog.setContentView(R.layout.dialog_time)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(timeFinishedDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        timeFinishedDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timeFinishedDialog.window!!.attributes = layoutParams

        timeFinishedDialog.findViewById<Button>(R.id.watch).setOnClickListener {
            rewarded(
                onRewDone = {
                    sketchViewModel.onEvent(
                        SketchUiEvent.ShowAndHideTimeFinishedDialog(false)
                    )

                    binding.theDrawingIsReadyBtn.visibility = View.GONE
                    sketchViewModel.onEvent(
                        SketchUiEvent.StartAndStopCountdownTimer(true)
                    )
                }
            )
        }
    }

    private fun rewarded(onRewDone: () -> Unit) {
        rewardedManger.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManger.OnAdClosedListener {
                override fun onRewClosed() {
                    onRewDone()
                }

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@SketchActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRewComplete() {}
            },
            isUnlockImages = false,
            onOpenPaywall = {
                Intent(
                    this, PaywallActivity::class.java
                ).also(::startActivity)
            }
        )
    }


    private lateinit var takePhotoDialog: Dialog
    private fun takePhotoDialog() {
        takePhotoDialog = Dialog(this)
        takePhotoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        takePhotoDialog.setCancelable(true)
        takePhotoDialog.setContentView(R.layout.dialog_take_photo)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(takePhotoDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        takePhotoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        takePhotoDialog.window!!.attributes = layoutParams

        val itsNotFinished = takePhotoDialog.findViewById<TextView>(R.id.its_not_finished)
        itsNotFinished.paintFlags = itsNotFinished.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        takePhotoDialog.findViewById<TextView>(R.id.its_not_finished).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideTakePhotoDialog(false)
            )
        }

        takePhotoDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideTakePhotoDialog(false)
            )
        }

        takePhotoDialog.findViewById<Button>(R.id.take_photo).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideTakePhotoDialog(false)
            )

            // event undone
            takePhoto()
        }
    }

    private lateinit var savePhotoDialog: Dialog
    private fun savePhotoDialog() {
        savePhotoDialog = Dialog(this)
        savePhotoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        savePhotoDialog.setCancelable(true)
        savePhotoDialog.setContentView(R.layout.dialog_save_photo)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(savePhotoDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        savePhotoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        savePhotoDialog.window!!.attributes = layoutParams

        val takeAnotherPhoto = savePhotoDialog.findViewById<TextView>(R.id.take_another_photo)
        takeAnotherPhoto.paintFlags = takeAnotherPhoto.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        savePhotoDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideSavePhotoDialog(false)
            )
        }

        savePhotoDialog.findViewById<TextView>(R.id.take_another_photo).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideSavePhotoDialog(false)
            )

            // event undone
            takePhoto()
        }

        savePhotoDialog.findViewById<Button>(R.id.save_photo).setOnClickListener {
            sketchViewModel.onEvent(
                SketchUiEvent.ShowAndHideSavePhotoDialog(false)
            )

            // undone event
            savePhoto()
        }
    }

    private fun takePhoto() {
        getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
            ImagePicker.with(this)
                .cameraOnly()
                .saveDir(it1)
                .createIntent { intent ->
                    startForTakeAndSaveDrawingPhotoResult.launch(intent)
                }
        }
    }

    private val startForTakeAndSaveDrawingPhotoResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri = result.data?.data!!

                Glide.with(this)
                    .asBitmap()
                    .load(uri.toString())
                    .into(
                        object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap, transition: Transition<in Bitmap>?
                            ) {
                                SketchBitmap.bitmapToSave = resource

                                sketchViewModel.onEvent(
                                    SketchUiEvent.ShowAndHideSavePhotoDialog(true)
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        }
                    )
            } else {
                Toast.makeText(
                    this, getString(R.string.error_importing_photo), Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun savePhoto() {
        if (SketchBitmap.bitmapToSave != null) {
            lifecycleScope.launch {
                val isSaved = async {
                    creationRepository.insertPhotoCreation(SketchBitmap.bitmapToSave)
                }

                val progressDialog = ProgressDialog(this@SketchActivity)
                progressDialog.setMessage(getString(R.string.saving_image))
                progressDialog.setCancelable(false)
                progressDialog.show()

                Toast.makeText(
                    this@SketchActivity,
                    if (isSaved.await()) getString(R.string.photo_saved)
                    else getString(R.string.something_went_wrong_while_saving_photo),
                    Toast.LENGTH_SHORT
                ).show()

                if (isSaved.await()) {
                    SketchBitmap.bitmapToSave = null
                    progressDialog.dismiss()
                    inAppReview()
                }
            }
        } else {
            Toast.makeText(
                this@SketchActivity,
                getString(R.string.something_went_wrong_while_saving_photo),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun inAppReview() {
        val manager = ReviewManagerFactory.create(this)

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result

                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    super.onBackPressed()
                }

            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isCameraGranted(this)) {
            binding.cameraView.open()
            binding.cameraView.clearFocus()
            setupCameraCallbacks()

        } else {
            PermissionUtils.checkPermission(
                this,
                Manifest.permission.CAMERA,
                CAMERA_PERMISSIONS_CODE
            )
        }

        if (SketchBitmap.bitmap != null) {
            binding.objImage.setImageBitmap(SketchBitmap.bitmap)
        }

        setSubscribedUser()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (
            requestCode == CAMERA_PERMISSIONS_CODE
            && (grantResults.isEmpty() || grantResults[0] != 0)
        ) {
            Toast.makeText(
                this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        if (requestCode != CAMERA_PERMISSIONS_CODE) {
            super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
            )
        }
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.close()

        if (isRecording) {
            stopVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appDataState?.isSubscribed == false) {
            videoHandler?.removeCallbacks(videoTimerRunnable)
            sketchViewModel.onEvent(
                SketchUiEvent.StartAndStopCountdownTimer(false)
            )
            SketchBitmap.bitmap = null
            SketchBitmap.borderedBitmap = null
        }
    }

    companion object {
        const val CAMERA_PERMISSIONS_CODE = 3002

        fun flip(bitmap: Bitmap?): Bitmap? {
            if (bitmap != null) {
                val matrix = Matrix()
                matrix.preScale(-1.0f, 1.0f)
                return Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            }
            return null
        }

        fun getBitmapWithTransparentBG(bitmap: Bitmap, color: Int): Bitmap {
            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val width = copy.width
            val height = copy.height
            for (i in 0 until height) {
                for (j in 0 until width) {
                    if (copy.getPixel(j, i) == color) {
                        copy.setPixel(j, i, 0)
                    }
                }
            }
            return copy
        }
    }


}
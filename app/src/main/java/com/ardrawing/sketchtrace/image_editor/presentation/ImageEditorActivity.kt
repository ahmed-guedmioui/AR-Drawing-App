package com.ardrawing.sketchtrace.image_editor.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityAdvancedBinding
import com.ardrawing.sketchtrace.image_editor.presentation.util.EditedBitmap
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.Constants
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.ardrawing.sketchtrace.sketch.presentation.util.SketchBitmap
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class ImageEditorActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private val imageEditorViewModel: ImageEditorViewModel by viewModels()
    private var imageEditorState: ImageEditorState? = null

    private lateinit var binding: ActivityAdvancedBinding

    private var edgeJob: Job? = null
    private var contrastJob: Job? = null
    private var noiseJob: Job? = null
    private var sharpnessJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                imageEditorViewModel.advancedEditingState.collect {
                    imageEditorState = it

                    if (imageEditorState?.appData?.isSubscribed == true) {
                        binding.vipApply.visibility = View.GONE
                    }

                    updatedSelected()
                }
            }
        }

        binding.objImage.setImageBitmap(SketchBitmap.bitmap)

        binding.edge.setOnClickListener {
            imageEditorViewModel.onEvent(ImageEditorUiEvent.Select(1))
        }

        binding.contrast.setOnClickListener {
            imageEditorViewModel.onEvent(ImageEditorUiEvent.Select(2))
        }

        binding.noise.setOnClickListener {
            imageEditorViewModel.onEvent(ImageEditorUiEvent.Select(3))
        }

        binding.sharpness.setOnClickListener {
            imageEditorViewModel.onEvent(ImageEditorUiEvent.Select(4))
        }

        binding.edgeSeek.setOnSeekBarChangeListener(this)
        binding.contrastSeek.setOnSeekBarChangeListener(this)
        binding.noiseSeek.setOnSeekBarChangeListener(this)
        binding.sharpnessSeek.setOnSeekBarChangeListener(this)

        binding.apply.setOnClickListener {
            if (imageEditorState?.appData?.isSubscribed == true) {
                SketchBitmap.bitmap = EditedBitmap.editedBitmap
                EditedBitmap.editedBitmap = null

                finish()
                Toast.makeText(
                    this, getString(R.string.applied), Toast.LENGTH_SHORT
                ).show()
            } else {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
    }

    private fun showApplyAlertDialog() {

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.do_you_want_to_apply_the_editing)
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                if (imageEditorState?.appData?.isSubscribed == true) {
                    SketchBitmap.bitmap = EditedBitmap.editedBitmap
                    EditedBitmap.editedBitmap = null

                    finish()
                    Toast.makeText(
                        this, getString(R.string.applied), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Intent(this, PaywallActivity::class.java).also {
                        startActivity(it)
                    }
                }
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                finish()
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    private fun updatedSelected() {
        when (imageEditorState?.selected) {
            0 -> {
                resetAllCardsColor()
            }

            1 -> {
                resetAllCardsColor()
                binding.edgeCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.edgeSeek.visibility = View.VISIBLE
            }

            2 -> {
                resetAllCardsColor()
                binding.contrastCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.contrastSeek.visibility = View.VISIBLE
            }

            3 -> {
                resetAllCardsColor()
                binding.noiseCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.noiseSeek.visibility = View.VISIBLE
            }

            4 -> {
                resetAllCardsColor()
                binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.sharpnessSeek.visibility = View.VISIBLE
            }
        }
    }

    private fun resetAllCardsColor() {
        binding.edgeSeek.visibility = View.GONE
        binding.contrastSeek.visibility = View.GONE
        binding.noiseSeek.visibility = View.GONE
        binding.sharpnessSeek.visibility = View.GONE

        binding.edgeCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.contrastCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.noiseCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.gray))
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        EditedBitmap.editedBitmap = SketchBitmap.bitmap
        setPreviousEdited()
        sendSelected(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private fun setPreviousEdited() {
        imageEditorState?.let {
            when (it.selected) {
                1 -> {
                    // apply the other filters except edge

                    if (it.isContrast) {
                        contrast()
                    }
                    if (it.isNoise) {
                        noise()
                    }
                    if (it.isSharpened) {
                        sharpen()
                    }
                }

                2 -> {
                    // apply the other filters except contrast

                    if (it.isEdged) {
                        edge()
                    }
                    if (it.isNoise) {
                        noise()
                    }
                    if (it.isSharpened) {
                        sharpen()
                    }

                }

                3 -> {
                    // apply the other filters except noise

                    if (it.isEdged) {
                        edge()
                    }
                    if (it.isContrast) {
                        contrast()
                    }
                    if (it.isSharpened) {
                        sharpen()
                    }
                }

                4 -> {
                    // apply the other filters except sharpness

                    if (it.isEdged) {
                        edge()
                    }
                    if (it.isContrast) {
                        contrast()
                    }
                    if (it.isNoise) {
                        noise()
                    }
                }
            }
        }
    }

    private fun sendSelected(level: Int) {
        when (imageEditorState?.selected) {
            1 -> {
                imageEditorViewModel.onEvent(
                    ImageEditorUiEvent.SetEdge(level)
                )
                edge()
            }

            2 -> {
                imageEditorViewModel.onEvent(
                    ImageEditorUiEvent.SetContrast(level)
                )
                contrast()
            }

            3 -> {
                imageEditorViewModel.onEvent(
                    ImageEditorUiEvent.SetNoise(level)
                )
                noise()
            }

            4 -> {
                imageEditorViewModel.onEvent(
                    ImageEditorUiEvent.SetSharpness(level)
                )
                sharpen()
            }
        }
    }

    private fun edge() {
        edgeJob?.cancel()
        edgeJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@ImageEditorActivity)
                gPUImage.setImage(EditedBitmap.editedBitmap)

                gPUImage.setFilter(
                    GPUImageSharpenFilter(
                        range(
                            imageEditorState?.edge ?: 0,
                            0.0f, 4.0f
                        )
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    EditedBitmap.editedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(EditedBitmap.editedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun contrast() {
        contrastJob?.cancel()
        contrastJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@ImageEditorActivity)
                gPUImage.setImage(EditedBitmap.editedBitmap)

                gPUImage.setFilter(
                    GPUImageContrastFilter(
                        range(
                            imageEditorState?.contrast ?: 0,
                            1.0f, 4.0f
                        )
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    EditedBitmap.editedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(EditedBitmap.editedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun noise() {
        noiseJob?.cancel()
        noiseJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@ImageEditorActivity)
                gPUImage.setImage(EditedBitmap.editedBitmap)

                val filter = GPUImageGaussianBlurFilter()
                filter.setBlurSize(
                    range(
                        imageEditorState?.noise ?: 0,
                        0.0f, 1.0f
                    )
                )

                gPUImage.setFilter(filter)

                if (gPUImage.bitmapWithFilterApplied != null) {
                    EditedBitmap.editedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(EditedBitmap.editedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun sharpen() {
        sharpnessJob?.cancel()
        sharpnessJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@ImageEditorActivity)
                gPUImage.setImage(EditedBitmap.editedBitmap)

                gPUImage.setFilter(
                    GPUImageSharpenFilter(
                        range(
                            imageEditorState?.sharpness ?: 0,
                            0.0f, 2.0f
                        )
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    EditedBitmap.editedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(EditedBitmap.editedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun range(percentage: Int, start: Float, end: Float): Float {
        val finePercentage = percentage / 2
        return (end - start) * finePercentage.toFloat() / 100.0f + start
    }

    override fun onResume() {
        super.onResume()
        imageEditorViewModel.onEvent(ImageEditorUiEvent.UpdateAppData)
        if (imageEditorState?.appData?.isSubscribed == true) {
            binding.vipApply.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        showApplyAlertDialog()
    }
}
















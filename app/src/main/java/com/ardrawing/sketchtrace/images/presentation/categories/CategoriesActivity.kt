package com.ardrawing.sketchtrace.images.presentation.categories

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.data.util.ads_original.InterstitialAdManager
import com.ardrawing.sketchtrace.core.data.util.ads_original.RewardedAdsManager
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterstitialManger
import com.ardrawing.sketchtrace.core.domain.repository.ads.NativeManager
import com.ardrawing.sketchtrace.core.domain.repository.ads.RewardedManger
import com.ardrawing.sketchtrace.databinding.ActivityCategoriesBinding
import com.ardrawing.sketchtrace.images.presentation.category.CategoryActivity
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.other_util.AppConstant
import com.ardrawing.sketchtrace.util.other_util.FileUtils
import com.ardrawing.sketchtrace.util.other_util.HelpActivity
import com.ardrawing.sketchtrace.util.other_util.HelpActivity2
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {

    private var storagePermissionRequestCode = 12

    private val categoriesViewModel: CategoriesViewModel by viewModels()
    private var categoriesState: CategoriesState? = null

    private var categoriesAdapter: CategoriesAdapter? = null

    private lateinit var binding: ActivityCategoriesBinding

    @Inject
    lateinit var rewardedManger: RewardedManger

    @Inject
    lateinit var interstitialManger: InterstitialManger

    @Inject
    lateinit var nativeManager: NativeManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        val bundle = intent.extras
        if (bundle != null) {
            val isTrace = bundle.getBoolean("isTrace", true)
            categoriesViewModel.onEvent(CategoriesUiEvents.UpdateIsTrace(isTrace))
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoriesViewModel.categoriesState.collect {
                    categoriesState = it

                    Log.d(
                        "tag_images",
                        "categoriesState.collect: ${categoriesState?.imageCategoryList?.size}"
                    )

                    categoriesAdapter?.imageCategoryList =
                        categoriesState?.imageCategoryList ?: emptyList()
                    categoriesAdapter?.notifyDataSetChanged()

                    if (categoriesState?.isTrace == true) {
                        binding.title.text = getString(R.string.trace)
                    } else {
                        binding.title.text = getString(R.string.sketch)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoriesViewModel.navigateToDrawingChannel.collect { navigate ->
                    if (navigate) {
                        categoriesState?.clickedImageItem?.let { clickedImageItem ->
                            if (categoriesState?.isTrace == true) {
                                traceDrawingScreen(clickedImageItem.image)
                            } else {
                                sketchDrawingScreen(clickedImageItem.image)
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoriesViewModel.unlockImageChannel.collect { unlock ->
                    if (unlock) {
                        rewarded {
                            categoriesViewModel.onEvent(CategoriesUiEvents.UnlockImage)
                        }
                    }
                }
            }
        }


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        categoriesAdapter = CategoriesAdapter(
            activity = this,
            nativeManager = nativeManager
        )

        initAdapterClicks()
        binding.recyclerView.adapter = categoriesAdapter


        binding.back.setOnClickListener {
            super.onBackPressed()
        }

        val pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.relHelp.setOnClickListener {
            it.startAnimation(pushAnimation)
            if (categoriesState?.isTrace == true) {
                helpScreen()
            } else {
                helpScreen2()
            }
        }

        writeStoragePermission()
    }

    private fun initAdapterClicks() {

        categoriesAdapter?.setClickListener(object : CategoriesAdapter.ClickListener {
            override fun oClick(categoryPosition: Int, imagePosition: Int) {
                categoriesViewModel.onEvent(
                    CategoriesUiEvents.OnImageClick(
                        categoryPosition = categoryPosition,
                        imagePosition = imagePosition
                    )
                )
            }
        })

        categoriesAdapter?.setGalleryAndCameraClickListener(object :
            CategoriesAdapter.GalleryAndCameraClickListener {
            override fun oClick(isGallery: Boolean) {
                categoriesViewModel.onEvent(
                    CategoriesUiEvents.UpdateIsGallery(isGallery)
                )
                rewarded {
                    if (isWriteStoragePermissionGranted()) {
                        if (categoriesState?.isGallery == true) {
                            ImagePicker.with(this@CategoriesActivity)
                                .galleryOnly()
                                .createIntent { intent ->
                                    startForProfileImageResult.launch(intent)
                                }
                        } else {
                            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                                ImagePicker.with(this@CategoriesActivity)
                                    .cameraOnly()
                                    .saveDir(it1)
                                    .createIntent { intent ->
                                        startForProfileImageResult.launch(intent)
                                    }
                            }
                        }

                    }
                }
            }
        })

        categoriesAdapter?.setViewMoreClickListener(
            object : CategoriesAdapter.ViewMoreClickListener {
                override fun oClick(categoryPosition: Int) {

//                    InterstitialAdManager.showInterstitial(
//                        this@CategoriesActivity,
//                        object : InterstitialAdManager.OnAdClosedListener {
//                            override fun onAdClosed() {
//                                Intent(
//                                    this@CategoriesActivity,
//                                    CategoryActivity::class.java
//                                ).also { intent ->
//                                    intent.putExtra("categoryPosition", categoryPosition)
//                                    intent.putExtra("isTrace", categoriesState?.isTrace)
//                                    startActivity(intent)
//                                }
//                            }
//                        }
//                    )

                    interstitialManger.showInterstitial(
                        this@CategoriesActivity,
                        object : InterstitialManger.OnAdClosedListener {
                            override fun onAdClosed() {
                                Intent(
                                    this@CategoriesActivity,
                                    CategoryActivity::class.java
                                ).also { intent ->
                                    intent.putExtra("categoryPosition", categoryPosition)
                                    intent.putExtra("isTrace", categoriesState?.isTrace)
                                    startActivity(intent)
                                }
                            }
                        }
                    )

                }
            }
        )
    }

    private fun rewarded(
        onRewComplete: () -> Unit
    ) {

//        RewardedAdsManager.showRewarded(
//            activity = this,
//            adClosedListener = object : RewardedAdsManager.OnAdClosedListener {
//                override fun onRewClosed() {}
//
//                override fun onRewFailedToShow() {
//                    Toast.makeText(
//                        this@CategoriesActivity,
//                        getString(R.string.ad_is_not_loaded_yet),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//
//                override fun onRewComplete() {
//                    onRewComplete()
//                }
//
//            },
//            onOpenPaywall = {
//                Intent(
//                    this, PaywallActivity::class.java
//                ).also {
//                    startActivity(it)
//                }
//            }
//        )

        rewardedManger.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManger.OnAdClosedListener {
                override fun onRewClosed() {}

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@CategoriesActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRewComplete() {
                    onRewComplete()
                }
            },
            onOpenPaywall = {
                Intent(
                    this, PaywallActivity::class.java
                ).also {
                    startActivity(it)
                }
            }
        )
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        if (
            checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("tag_per", "isWriteStoragePermissionGranted: true")
            return true
        }

        Log.d("tag_per", "isWriteStoragePermissionGranted: requestPermissions")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.CAMERA"
            ),
            storagePermissionRequestCode
        )
        return false
    }

    private fun writeStoragePermission() {
        if (
            checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ActivityCompat.requestPermissions(
            this, arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_IMAGES"
            ), 20011
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != storagePermissionRequestCode) {
            return
        }

        if (grantResults.isEmpty() || grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
            return
        }

        if (categoriesState?.isGallery == true) {
            ImagePicker.with(this)
                .galleryOnly()
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        } else {
            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                ImagePicker.with(this@CategoriesActivity)
                    .cameraOnly()
                    .saveDir(it1)
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }
            }
        }
    }

    private fun helpScreen() {
        startActivity(Intent(this, HelpActivity2::class.java))
    }

    private fun helpScreen2() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = result.data?.data!!

                val selectedImagePath = if (categoriesState?.isGallery == true) {
                    AppConstant.getRealPathFromURI_API19(this, fileUri)
                } else {
                    FileUtils.getPath(fileUri)
                }

                if (selectedImagePath != null) {
                    if (categoriesState?.isTrace == true) {
                        traceDrawingScreen(selectedImagePath)
                    } else {
                        sketchDrawingScreen(selectedImagePath)
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_picking_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    this,
                    getString(R.string.error_picking_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun traceDrawingScreen(imagePath: String) {
//        InterstitialAdManager.showInterstitial(
//            this,
//            object : InterstitialAdManager.OnAdClosedListener {
//                override fun onAdClosed() {
//                    Intent(
//                        this@CategoriesActivity, TraceActivity::class.java
//                    ).also {
//                        it.putExtra("imagePath", imagePath)
//                        startActivity(it)
//                    }
//                }
//            }
//        )

        interstitialManger.showInterstitial(
            this,
            object : InterstitialManger.OnAdClosedListener {
                override fun onAdClosed() {
                    Intent(
                        this@CategoriesActivity, TraceActivity::class.java
                    ).also {
                        it.putExtra("imagePath", imagePath)
                        startActivity(it)
                    }
                }
            }
        )
    }

    private fun sketchDrawingScreen(imagePath: String) {
//        InterstitialAdManager.showInterstitial(
//            this,
//            object : InterstitialAdManager.OnAdClosedListener {
//                override fun onAdClosed() {
//                    Intent(
//                        this@CategoriesActivity, SketchActivity::class.java
//                    ).also {
//                        it.putExtra("imagePath", imagePath)
//                        startActivity(it)
//                    }
//                }
//            }
//        )

        interstitialManger.showInterstitial(
            this,
            object : InterstitialManger.OnAdClosedListener {
                override fun onAdClosed() {
                    Intent(
                        this@CategoriesActivity, SketchActivity::class.java
                    ).also {
                        it.putExtra("imagePath", imagePath)
                        startActivity(it)
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        categoriesViewModel.onEvent(CategoriesUiEvents.UpdateAppData)
    }

}

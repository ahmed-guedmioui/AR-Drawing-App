package com.ardrawing.sketchtrace.image_list.presentation.category

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityCategoryBinding
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {

    private var categoryAdapter: CategoryAdapter? = null

    private val categoryViewModel: CategoryViewModel by viewModels()
    private var categoryState: CategoryState? = null

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)


        val bundle = intent.extras
        if (bundle != null) {
            val categoryPosition = bundle.getInt("categoryPosition", 0)
            val isTrace = bundle.getBoolean("isTrace", true)

            categoryViewModel.onEvent(
                CategoryUiEvents.UpdateCategoryPositionAndIsTrace(
                    categoryPosition = categoryPosition,
                    isTrace = isTrace
                )
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.categoryState.collect {
                    categoryState = it

                    categoryAdapter?.notifyDataSetChanged()

                    binding.title.text =
                        categoryState?.imageCategory?.imageCategoryName


                    if (categoryState?.isTrace == true) {
                        binding.title.text = getString(R.string.trace)
                    } else {
                        binding.title.text = getString(R.string.sketch)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.appData.collect { appData ->
                    NativeManager.loadNative(
                        appData,
                        findViewById(R.id.native_frame),
                        findViewById(R.id.native_temp),
                        this@CategoryActivity, false
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                categoryViewModel.navigateToDrawingChannel.collect { navigate ->
                    if (navigate) {
                        categoryState?.clickedImageItem?.let { clickedImageItem ->
                            if (categoryState?.isTrace == true) {
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
                categoryViewModel.unlockImageChannel.collect { unlock ->
                    if (unlock) {
                        rewarded {
                            categoryViewModel.onEvent(CategoryUiEvents.UnlockImage)

                            categoryState?.imagePosition?.let {
                                categoryAdapter?.notifyItemChanged(it)
                            }
                        }
                    }
                }
            }
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.layoutManager = gridLayoutManager


        categoryAdapter = CategoryAdapter(
            activity = this,
            imageList = categoryState?.imageCategory?.imageList ?: emptyList(),
            from = 2
        )

        categoryAdapter?.setClickListener(object : CategoryAdapter.ClickListener {
            override fun oClick(imagePosition: Int) {

                categoryViewModel.onEvent(
                    CategoryUiEvents.OnImageClick(
                        imagePosition = imagePosition
                    )
                )
            }
        })

        binding.recyclerView.adapter = categoryAdapter
    }


    private fun rewarded(
        onRewComplete: () -> Unit
    ) {
        RewardedManager.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManager.OnAdClosedListener {
                override fun onRewClosed() {}

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@CategoryActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRewComplete() {
                    onRewComplete()
                }

            },
            onOpenPaywall = {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
            }
        )
    }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoryActivity, TraceActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

    private fun sketchDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoryActivity, SketchActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

}

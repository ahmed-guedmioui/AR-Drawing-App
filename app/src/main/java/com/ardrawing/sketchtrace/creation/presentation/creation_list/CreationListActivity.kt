package com.ardrawing.sketchtrace.creation.presentation.creation_list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ardrawing.sketchtrace.core.data.util.ads_original.InterstitialAdManager
import com.ardrawing.sketchtrace.databinding.ActivityMyCreationLsitBinding
import com.ardrawing.sketchtrace.creation.presentation.creation_details.CreationDetailsActivity
import com.ardrawing.sketchtrace.creation.presentation.creation_list.adapter.CreationListAdapter
import com.ardrawing.sketchtrace.core.domain.repository.ads.InterstitialManger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.ardrawing.sketchtrace.language.data.util.LanguageChanger
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CreationListActivity : AppCompatActivity() {

    @Inject
    lateinit var interstitialManger: InterstitialManger

    private val creationListViewModel: CreationListViewModel by viewModels()
    private lateinit var creationListState: CreationListState

    private lateinit var binding: ActivityMyCreationLsitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageChanger.changeAppLanguage(this)
        binding = ActivityMyCreationLsitBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                creationListViewModel.myCreationState.collect {
                    creationListState = it
                    initCreationListRec()
                }
            }
        }

    }

    private fun initCreationListRec() {
        val creationListAdapter = CreationListAdapter(
            this, creationListState.creationList
        )

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = creationListAdapter

        creationListAdapter.setClickListener(
            object : CreationListAdapter.ClickListener {
                override fun oClick(uri: String, isVideo: Boolean) {

//                InterstitialAdManager.showInterstitial(
//                    this@CreationListActivity,
//                    object : InterstitialAdManager.OnAdClosedListener {
//                        override fun onAdClosed() {
//                            Intent(
//                                this@CreationListActivity,
//                                CreationDetailsActivity::class.java
//                            ).also { intent ->
//                                intent.putExtra("uri", uri)
//                                intent.putExtra("isVideo", isVideo)
//                                startActivity(intent)
//                                finish()
//                            }
//                        }
//                    }
//                )

                    interstitialManger.showInterstitial(
                        this@CreationListActivity,
                        object : InterstitialManger.OnAdClosedListener {
                            override fun onAdClosed() {
                                Intent(
                                    this@CreationListActivity,
                                    CreationDetailsActivity::class.java
                                ).also { intent ->
                                    intent.putExtra("uri", uri)
                                    intent.putExtra("isVideo", isVideo)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    )
                }
            }
        )

    }

}
















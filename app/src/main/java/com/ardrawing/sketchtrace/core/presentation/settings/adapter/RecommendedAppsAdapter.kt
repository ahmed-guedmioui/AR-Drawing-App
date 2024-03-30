package com.ardrawing.sketchtrace.core.presentation.settings.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.util.rateApp
import com.bumptech.glide.Glide

/**
 * @author Ahmed Guedmioui
 */
class RecommendedAppsAdapter(
    private val activity: Activity,
    private val appData: AppData
) : RecyclerView.Adapter<RecommendedAppsAdapter.ViewHolder>() {

    override fun getItemViewType(i: Int): Int {
        return i
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(
            activity = activity,
            appData = appData,
            view = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.item_recommended_app, null
            )
        )
    }

    override fun onBindViewHolder(listHolder: ViewHolder, i: Int) {
        listHolder.bind(i)
    }

    override fun getItemCount(): Int {
        return appData.recommendedApps.size
    }


    class ViewHolder(
        private val activity: Activity,
        private val appData: AppData,
        private val view: View
    ) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            try {

                Glide.with(activity)
                    .load(
                        appData.recommendedApps[position].image
                    )
                    .into(
                        view.findViewById(R.id.app_cover)
                    )

                Glide.with(activity)
                    .load(
                        appData.recommendedApps[position].icon
                    )
                    .into(
                        view.findViewById(R.id.app_icon)
                    )

                view.findViewById<TextView>(R.id.app_title).text =
                    appData.recommendedApps[position].name

                view.findViewById<TextView>(R.id.app_desc).text =
                    appData.recommendedApps[position].shortDescription

                view.setOnClickListener {
                    rateApp(
                        activity = activity,
                        packageName = appData.recommendedApps[position].urlOrPackage
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
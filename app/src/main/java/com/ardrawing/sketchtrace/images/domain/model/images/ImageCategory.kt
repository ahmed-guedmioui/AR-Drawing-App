package com.ardrawing.sketchtrace.images.domain.model.images

import androidx.recyclerview.widget.RecyclerView
import com.ardrawing.sketchtrace.images.presentation.category.CategoryAdapter

data class ImageCategory(
    val imageCategoryName: String,
    val categoryId: Int,
    val imageList: List<Image>,

    var adapter: CategoryAdapter? = null,
    var recyclerView: RecyclerView? = null
)
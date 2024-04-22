package com.ardrawing.sketchtrace.settings.domain.model

data class RecommendedApp(
    val icon: String,
    val image: String,
    val name: String,
    val shortDescription: String,
    val urlOrPackage: String
)
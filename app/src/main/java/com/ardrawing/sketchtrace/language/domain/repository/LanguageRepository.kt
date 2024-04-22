package com.ardrawing.sketchtrace.language.domain.repository

/**
 * @author Ahmed Guedmioui
 */
interface LanguageRepository {
    fun getLanguageCode(): String
    fun updateLanguageCode(code: String)
}
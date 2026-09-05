package com.healthyscan.app.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Wraps the AndroidX per-app language API (AppCompatDelegate.setApplicationLocales).
 * This works on all supported API levels (26+) thanks to the AppCompat backport,
 * and maps to the platform per-app language feature natively on API 33+.
 */
object LocaleManager {

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_GREEK = "el"

    fun setAppLanguage(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun currentLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) LANGUAGE_ENGLISH else locales[0]?.language ?: LANGUAGE_ENGLISH
    }

    fun toggleLanguage() {
        val next = if (currentLanguage() == LANGUAGE_GREEK) LANGUAGE_ENGLISH else LANGUAGE_GREEK
        setAppLanguage(next)
    }
}

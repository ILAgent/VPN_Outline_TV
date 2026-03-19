package com.ilagent.nativeoutline.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LanguageContextWrapper(base: Context) : ContextWrapper(base) {
    
    companion object {
        fun wrap(context: Context, languageCode: String?): Context {
            if (languageCode == null || languageCode == "system") {
                return context
            }
            
            val config = Configuration(context.resources.configuration)
            val locale = when (languageCode) {
                "zh-rCN" -> Locale("zh", "CN")
                "zh-rTW" -> Locale("zh", "TW")
                else -> Locale(languageCode)
            }
            
            Locale.setDefault(locale)
            config.setLocale(locale)
            
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(config, context.resources.displayMetrics)
                context
            }
        }
    }
}
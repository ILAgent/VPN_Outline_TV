package com.ilagent.nativeoutline.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.ilagent.nativeoutline.utils.CrashlyticsLogger

class VpnWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = VpnWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        CrashlyticsLogger.logWidgetAdded()
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        CrashlyticsLogger.logWidgetRemoved()
    }
}
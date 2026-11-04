package com.sujal.expensetracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Pings the home-screen widget to refresh whenever money-affecting data changes. */
object WidgetUpdater {
    fun notifyChanged(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, MoneyWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val intent = Intent(context, MoneyWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}

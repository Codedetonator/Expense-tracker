package com.sujal.expensetracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sujal.expensetracker.MainActivity
import com.sujal.expensetracker.R
import com.sujal.expensetracker.data.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.DecimalFormat

/** Home-screen widget: total money (cash + online) and today's spend, with a quick-add
 * button that jumps straight to the "new expense" sheet. Refreshed via [WidgetUpdater]
 * whenever a balance-affecting change happens in the app, and on Android's own schedule.
 * Uses one-shot DB reads (not the app's LiveData) since this can run before anything in
 * the app process is observing that data. */
class MoneyWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val moneyFormat = DecimalFormat("₹#,##0.##")

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        scope.launch {
            try {
                val repo = ExpenseRepository(context.applicationContext)
                val total = repo.getTotalMoneyOnce()
                val todaySpent = repo.getTodaySpentOnce()
                for (id in appWidgetIds) {
                    updateOne(context, appWidgetManager, id, total, todaySpent)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun updateOne(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        total: Double,
        todaySpent: Double
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_money)
        views.setTextViewText(R.id.widget_text_total, moneyFormat.format(total))
        views.setTextViewText(R.id.widget_text_today, "Spent today: ${moneyFormat.format(todaySpent)}")

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPending = PendingIntent.getActivity(
            context, widgetId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_text_total, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_text_total_label, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_text_today, openAppPending)

        val addExpenseIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_ADD_SHEET, true)
        }
        val addExpensePending = PendingIntent.getActivity(
            context, widgetId + 100_000, addExpenseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_add, addExpensePending)

        manager.updateAppWidget(widgetId, views)
    }
}

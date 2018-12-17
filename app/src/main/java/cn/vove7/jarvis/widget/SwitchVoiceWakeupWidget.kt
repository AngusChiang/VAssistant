package cn.vove7.jarvis.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import cn.vove7.jarvis.R
import cn.vove7.jarvis.activities.base.VoiceAssistActivity

/**
 * # SwitchVoiceWakeupWidget
 *
 * @author Administrator
 * 2018/12/17
 */
@Deprecated("使用CreateShortcutActivity")
class SwitchVoiceWakeupWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.quick_widget)

            views.setTextViewText(R.id.appwidget_text, "语音唤醒")
            views.setOnClickPendingIntent(R.id.widget_layout,
                    PendingIntent.getActivity(context, 0,
                            Intent(context, VoiceAssistActivity::class.java)
                                    .also { it.action = Intent.ACTION_VOICE_COMMAND }, 0))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
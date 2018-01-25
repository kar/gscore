package gs.main

import com.github.salomonbrys.kodein.instance
import gs.environment.inject


internal fun registerUncaughtExceptionHandler(ctx: android.content.Context) {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
        gs.main.restartApplicationThroughService(ctx, 2000)
        defaultHandler.uncaughtException(thread, ex)
    }
}

private fun restartApplicationThroughService(ctx: android.content.Context, delayMillis: Int) {
    val alarm: android.app.AlarmManager = ctx.inject().instance()

    val restartIntent = android.content.Intent(ctx, AKeepAliveService::class.java)
    val intent = android.app.PendingIntent.getService(ctx, 0, restartIntent, 0)
    alarm.set(android.app.AlarmManager.RTC, System.currentTimeMillis() + delayMillis, intent)

    val j = ctx.inject().instance<gs.environment.Journal>()
    j.log("restarting app")
}

internal fun getPreferredLocales(): List<java.util.Locale> {
    val cfg = android.content.res.Resources.getSystem().configuration
    return try {
        // Android, a custom list type that is not an iterable. Just wow.
        val locales = cfg.locales
        (0..locales.size() - 1).map { locales.get(it) }
    } catch (t: Throwable) { listOf(cfg.locale) }
}

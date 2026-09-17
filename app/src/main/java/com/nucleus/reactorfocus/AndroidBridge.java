package com.nucleus.reactorfocus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.webkit.JavascriptInterface;

public class AndroidBridge {
    private static final int REQUEST_CODE = 4242;
    private final Context context;

    public AndroidBridge(Context context) {
        this.context = context.getApplicationContext();
    }

    private PendingIntent alarmIntent(String mode) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("mode", mode == null ? "focus" : mode);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }

    @JavascriptInterface
    public void scheduleAlarm(double epochMilliseconds, String mode) {
        long triggerAt = (long) epochMilliseconds;
        if (triggerAt <= System.currentTimeMillis()) return;

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = alarmIntent(mode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    @JavascriptInterface
    public void cancelAlarm() {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(alarmIntent("focus"));
    }
}

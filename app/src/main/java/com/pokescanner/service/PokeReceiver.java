package com.pokescanner.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.pokescanner.loaders.MultiAccountLoader;
import com.pokescanner.settings.Settings;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Brian on 7/26/2016.
 */

public class PokeReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    public static void cancelAlarm(Context context) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(PokeNotifications.ONGOING_ID);
        Settings.setPreference(context, Settings.ENABLE_SERVICE, false);
        MultiAccountLoader.cancelAllThreads();
        Intent intent = new Intent(context, PokeReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, PokeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Something");
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(context)
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        if (intent.getAction() != null) {
            if (intent.getAction().equals(PokeNotifications.STOP_SERVICE)) {
                cancelAlarm(context);
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                if (Settings.getPreferenceBoolean(context, Settings.ENABLE_SERVICE_ON_BOOT)) {
                    scheduleAlarm(context);
                }
            } else if (intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {
                if (Settings.getPreferenceBoolean(context, Settings.ENABLE_SERVICE)) {
                    scheduleAlarm(context);
                }
            } else if (intent.getAction().equals(PokeNotifications.RESCAN)) {
                Intent i = new Intent(context, PokeService.class);
                context.startService(i);
            }
        } else {
            Intent i = new Intent(context, PokeService.class);
            context.startService(i);
        }

    }

    public static void scheduleAlarm(Context context) {
        PokeNotifications.ongiongNotification("Service Started", context);

        Intent intent = new Intent(context, PokeReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, PokeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                Integer.parseInt(Settings.getPreferenceString(context, Settings.SERVICE_REFRESH)), pIntent);

    }
}

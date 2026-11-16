package com.attentiontwin.app;

import android.app.*;
import android.content.*;

/** Voluntary one-shot reminders; inexact alarms require no special alarm permission. */
public final class WellnessReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "wellness_reminders";
    static void schedule(Context context, int id, long at, String title, String text) {
        schedule(context,id,at,title,text,false);
    }
    static void schedule(Context context, int id, long at, String title, String text, boolean daily) {
        Intent intent = new Intent(context, WellnessReceiver.class).putExtra("id", id).putExtra("title", title).putExtra("text", text);
        PendingIntent pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        alarms.cancel(pending);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(id);
        java.util.Calendar date=java.util.Calendar.getInstance();date.setTimeInMillis(at);
        context.getSharedPreferences("wellness_reminders", 0).edit().putLong(id+"_at", at).putString(id+"_title", title).putString(id+"_text", text).putBoolean(id+"_daily",daily&&at>0).putInt(id+"_hour",date.get(java.util.Calendar.HOUR_OF_DAY)).putInt(id+"_minute",date.get(java.util.Calendar.MINUTE)).apply();
        if (at > System.currentTimeMillis()) {
            try {
                if(android.os.Build.VERSION.SDK_INT<31||alarms.canScheduleExactAlarms())alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending);
                else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending);
            } catch(SecurityException error){alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pending);}
        }
    }
    static void restore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("wellness_reminders", 0);
        for (int id : new int[]{901,902,903}) {
            long at = prefs.getLong(id+"_at",0);
            boolean daily=prefs.getBoolean(id+"_daily",false);
            if(daily)at=nextDaily(prefs,id);
            if (at > System.currentTimeMillis()) schedule(context,id,at,prefs.getString(id+"_title","Check in"),prefs.getString(id+"_text","Take a moment for yourself."),daily);
        }
    }
    @Override public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id",903);
        SharedPreferences prefs = context.getSharedPreferences("wellness_reminders",0);
        long dueAt = prefs.getLong(id+"_at",0);
        if (dueAt == 0 || dueAt > System.currentTimeMillis()) return;
        boolean daily=prefs.getBoolean(id+"_daily",false);
        if(daily)schedule(context,id,nextDaily(prefs,id),intent.getStringExtra("title"),intent.getStringExtra("text"),true);
        else prefs.edit().putLong(id+"_at",0).apply();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Wellness reminders",NotificationManager.IMPORTANCE_DEFAULT));
        Intent open = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        Notification.Builder notification = new Notification.Builder(context,CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_attention)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("text"))
            .setStyle(new Notification.BigTextStyle().bigText(intent.getStringExtra("text")))
            .setAutoCancel(true);
        if(open!=null) notification.setContentIntent(PendingIntent.getActivity(context,id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        try { manager.notify(id,notification.build()); } catch (SecurityException ignored) { }
    }
    private static long nextDaily(SharedPreferences prefs,int id){
        java.util.Calendar date=java.util.Calendar.getInstance();date.set(java.util.Calendar.HOUR_OF_DAY,prefs.getInt(id+"_hour",22));date.set(java.util.Calendar.MINUTE,prefs.getInt(id+"_minute",0));date.set(java.util.Calendar.SECOND,0);date.set(java.util.Calendar.MILLISECOND,0);
        if(date.getTimeInMillis()<=System.currentTimeMillis())date.add(java.util.Calendar.DAY_OF_YEAR,1);return date.getTimeInMillis();
    }
}

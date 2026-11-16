package com.attentiontwin.app;
import android.app.PendingIntent;
import android.appwidget.*;
import android.content.*;
import android.graphics.Color;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.*;

/** Small launcher widget with local goals, navigation shortcuts and an independent theme. */
public final class AttentionWidget extends AppWidgetProvider {
    static void updateAll(Context c){AppWidgetManager manager=AppWidgetManager.getInstance(c);int[] ids=manager.getAppWidgetIds(new ComponentName(c,AttentionWidget.class));for(int id:ids)render(c,manager,id);}
    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids){for(int id:ids)render(c,manager,id);}
    @Override public void onReceive(Context c,Intent intent){super.onReceive(c,intent);if("com.attentiontwin.WIDGET_THEME".equals(intent.getAction())){android.content.SharedPreferences p=c.getSharedPreferences("appearance",0);p.edit().putBoolean("widget_dark",!p.getBoolean("widget_dark",true)).apply();updateAll(c);}}
    private static PendingIntent route(Context c,String page,int code){Intent intent=new Intent(c,MainActivity.class).putExtra("at_page",page).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);return PendingIntent.getActivity(c,code,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private static void render(Context c,AppWidgetManager manager,int id){
        boolean dark=c.getSharedPreferences("appearance",0).getBoolean("widget_dark",true);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.attention_widget);int ink=Color.parseColor(dark?"#EBF4E6":"#233D32"),muted=Color.parseColor(dark?"#B4C6AB":"#596B50");
        v.setInt(R.id.widget_root,"setBackgroundResource",dark?R.drawable.widget_dark:R.drawable.widget_light);
        for(int view:new int[]{R.id.widget_title,R.id.widget_focus,R.id.widget_goals,R.id.widget_theme})v.setTextColor(view,ink);v.setTextColor(R.id.widget_status,muted);v.setTextColor(R.id.widget_usage,muted);
        long until=FocusUtils.focusUntil(c);String end=until>System.currentTimeMillis()&&until!=Long.MAX_VALUE?" · until "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(until)):"";
        v.setTextViewText(R.id.widget_status,(FocusUtils.isFilteringActive(c)?"Notification rules on":"Notification rules off")+end);
        android.content.SharedPreferences p=ScrollGuardService.prefs(c);boolean today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()).equals(p.getString("day",""));long ig=today?p.getLong("ig",0)/60000:0,yt=today?p.getLong("yt",0)/60000:0;
        v.setTextViewText(R.id.widget_usage,"Instagram "+ig+goal(p.getInt("ig_limit",0))+" min  ·  YouTube "+yt+goal(p.getInt("yt_limit",0))+" min");
        v.setTextViewText(R.id.widget_theme,dark?"Light widget":"Dark widget");v.setOnClickPendingIntent(R.id.widget_focus,route(c,"focus",820));v.setOnClickPendingIntent(R.id.widget_goals,route(c,"scroll",821));v.setOnClickPendingIntent(R.id.widget_title,route(c,"home",822));v.setOnClickPendingIntent(R.id.widget_theme,PendingIntent.getBroadcast(c,823,new Intent(c,AttentionWidget.class).setAction("com.attentiontwin.WIDGET_THEME"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));manager.updateAppWidget(id,v);
    }
    private static String goal(int minutes){return minutes>0?" / "+minutes:"";}
}

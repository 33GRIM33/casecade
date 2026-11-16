package com.attentiontwin.app;

import android.accessibilityservice.AccessibilityService;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.*;
import android.view.accessibility.*;
import android.widget.*;
import com.getcapacitor.JSObject;
import java.text.SimpleDateFormat;
import java.util.*;

/** Opt-in, local-only foreground/scroll reminders. Never reads screen text or typed content. */
public final class ScrollGuardService extends AccessibilityService {
    private static volatile ScrollGuardService instance;
    private final ScrollTracker tracker=new ScrollTracker();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private volatile View overlay;
    private boolean blockingOverlay;
    private boolean testingOverlay;
    private String lastGoal="",blockError="";
    private long extensionElapsedUntil;
    private String day="",lastScreen="Waiting for a watched app",lastPackage="";
    private long lastSave=0,lastScan=0,lastScroll=0,lastForegroundPoll=0;
    private boolean connected;
    private boolean budgetShown;
    private static final String IG="com.instagram.android",YT="com.google.android.youtube";
    static SharedPreferences prefs(Context c){return c.getSharedPreferences("scroll_guard",0);}
    private static String today(){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
    static boolean accessGranted(Context c){String enabled=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);if(enabled==null)return false;ComponentName target=new ComponentName(c,ScrollGuardService.class);for(String entry:enabled.split(":"))if(target.equals(ComponentName.unflattenFromString(entry)))return true;return false;}
    static JSObject state(Context c){
        SharedPreferences p=prefs(c);ScrollGuardService service=instance;boolean current=today().equals(p.getString("day",""));
        JSObject out=new JSObject().put("enabled",p.getBoolean("enabled",false)).put("accessGranted",accessGranted(c)).put("connected",service!=null&&service.connected).put("intervalSeconds",p.getInt("interval",300)).put("dailyBudgetMinutes",p.getInt("budget",0)).put("instagram",p.getBoolean("instagram",true)).put("youtube",p.getBoolean("youtube",true)).put("shortsOnly",p.getBoolean("shorts_only",false)).put("overlay",p.getBoolean("overlay",true)).put("pausedUntil",p.getLong("paused_until",0));
        out.put("consent",p.getBoolean("consent",false)).put("instagramLimitMinutes",p.getInt("ig_limit",0)).put("youtubeLimitMinutes",p.getInt("yt_limit",0)).put("limitMode",p.getString("limit_mode","remind")).put("extensionUntil",p.getLong("extension_until",0)).put("blockingVisible",service!=null&&service.blockingOverlay).put("blockError",service==null?"":service.blockError);
        return out.put("activeApp",service==null?"":service.tracker.active()).put("screenStatus",service==null?"Accessibility service is not connected":service.lastScreen).put("continuousSeconds",service==null?0:service.tracker.continuous()/1000).put("nextInSeconds",service==null?p.getInt("interval",300):service.tracker.nextIn()/1000).put("instagramSeconds",service==null?(current?p.getLong("ig",0)/1000:0):service.tracker.instagram()/1000).put("youtubeSeconds",service==null?(current?p.getLong("yt",0)/1000:0):service.tracker.youtube()/1000).put("scrollEvents",service==null?(current?p.getInt("scrolls",0):0):service.tracker.scrolls()).put("remindersToday",service==null?(current?p.getInt("reminders",0):0):service.tracker.reminders()).put("lastReminderAt",p.getLong("last_reminder",0)).put("overlayVisible",service!=null&&service.overlay!=null);
    }
    static void refresh(){ScrollGuardService service=instance;if(service!=null)service.handler.post(()->{service.tracker.configure(prefs(service).getInt("interval",300));service.hideOverlay();service.lastGoal="";if(!service.enabled())service.tracker.leave(SystemClock.elapsedRealtime());service.save();});}
    static boolean testReminder(){ScrollGuardService service=instance;if(service==null)return false;service.handler.post(()->service.remind(true));return true;}
    static boolean testBlock(){ScrollGuardService service=instance;if(service==null)return false;service.handler.post(()->service.showBlock("This is the pause screen shown when your goal is reached.",true));return true;}
    static void clear(Context c){prefs(c).edit().clear().apply();ScrollGuardService service=instance;if(service!=null)service.handler.post(()->{service.tracker.leave(SystemClock.elapsedRealtime());service.tracker.resetDay();service.hideOverlay();});}
    private boolean enabled(){SharedPreferences p=prefs(this);return p.getBoolean("enabled",false)&&p.getLong("paused_until",0)<=System.currentTimeMillis();}
    @Override protected void onServiceConnected(){super.onServiceConnected();instance=this;connected=true;day=today();SharedPreferences p=prefs(this);extensionElapsedUntil=SystemClock.elapsedRealtime()+Math.max(0,Math.min(300000,p.getLong("extension_until",0)-System.currentTimeMillis()));tracker.configure(p.getInt("interval",300));if(day.equals(p.getString("day","")))tracker.restore(p.getLong("ig",0),p.getLong("yt",0),p.getInt("scrolls",0),p.getInt("reminders",0));handler.post(tick);}
    private final Runnable tick=new Runnable(){@Override public void run(){
        try{
            if(!day.equals(today())){day=today();tracker.resetDay();budgetShown=false;lastGoal="";extensionElapsedUntil=0;prefs(ScrollGuardService.this).edit().remove("extension_until").apply();hideOverlay();save();}
            PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);boolean awake=power!=null&&power.isInteractive();
            if(enabled()&&awake&&!blockingOverlay&&SystemClock.elapsedRealtime()-lastForegroundPoll>=2000){lastForegroundPoll=SystemClock.elapsedRealtime();pollForeground();}
            if(!enabled()||!awake){tracker.leave(SystemClock.elapsedRealtime());if(!testingOverlay||!awake)hideOverlay();}
            else tracker.tick(SystemClock.elapsedRealtime(),!blockingOverlay);
            int budget=prefs(ScrollGuardService.this).getInt("budget",0);
            boolean budgetDue=budget>0&&!budgetShown&&(tracker.instagram()+tracker.youtube())>=budget*60000L;
            String goal=goal();boolean block=enabled()&&awake&&!goal.isEmpty()&&"block".equals(prefs(ScrollGuardService.this).getString("limit_mode","remind"));
            if(block){if(!blockingOverlay)showBlock(goal,false);}
            else if(enabled()&&awake&&!tracker.active().isEmpty()&&(tracker.due()||budgetDue||(!goal.isEmpty()&&!goal.equals(lastGoal)))&&overlay==null){if(budgetDue)budgetShown=true;lastGoal=goal;tracker.reminded();remind(false);}
            if(SystemClock.elapsedRealtime()-lastSave>5000)save();
        }catch(RuntimeException ignored){}finally{handler.postDelayed(this,1000);}
    }};
    private void pollForeground(){AccessibilityNodeInfo root=getRootInActiveWindow();if(root==null)return;try{CharSequence name=root.getPackageName();if(name==null)return;String pkg=name.toString();if(pkg.equals(getPackageName())&&overlay!=null)return;if((pkg.equals(IG)&&prefs(this).getBoolean("instagram",true))||(pkg.equals(YT)&&prefs(this).getBoolean("youtube",true))){if(!prefs(this).getBoolean("shorts_only",false)){tracker.enter(pkg,SystemClock.elapsedRealtime());lastScreen="Watching "+(pkg.equals(IG)?"Instagram":"YouTube")+" (whole-app mode)";}}else{tracker.leave(SystemClock.elapsedRealtime());hideOverlay();lastScreen="Outside watched apps";}}finally{root.recycle();}}
    private String goal(){SharedPreferences p=prefs(this);return ScrollLimitPolicy.reached(tracker.active(),tracker.instagram(),tracker.youtube(),p.getInt("budget",0),p.getInt("ig_limit",0),p.getInt("yt_limit",0),extensionElapsedUntil,SystemClock.elapsedRealtime());}
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(event==null||event.getPackageName()==null)return;
        String pkg=event.getPackageName().toString();long now=SystemClock.elapsedRealtime();
        // Observe package transitions only. Screen content is inspected solely as resource IDs in opt-in Shorts mode.
        if(pkg.equals(getPackageName())){if(overlay==null&&event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){tracker.leave(now);lastScreen="AttentionTwin is open";save();}return;}
        if(!enabled()){tracker.leave(now);hideOverlay();return;}
        boolean watched=(pkg.equals(IG)&&prefs(this).getBoolean("instagram",true))||(pkg.equals(YT)&&prefs(this).getBoolean("youtube",true));
        if(!watched){if(event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){tracker.leave(now);lastPackage="";lastScreen="Outside watched apps";hideOverlay();save();}return;}
        if(prefs(this).getBoolean("shorts_only",false)){
            if(now-lastScan<750&&pkg.equals(lastPackage)){if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_SCROLLED&&now-lastScroll>500){tracker.scrolled();lastScroll=now;}return;}
            lastScan=now;AccessibilityNodeInfo root=getRootInActiveWindow();boolean matched=false;
            if(root!=null){if(pkg.contentEquals(root.getPackageName()==null?"":root.getPackageName()))matched=hasFeedId(root,0);root.recycle();}
            if(!matched){tracker.leave(now);lastPackage=pkg;lastScreen="Reels/Shorts screen not identified; whole-app mode is more reliable";hideOverlay();return;}
        }
        tracker.enter(pkg,now);lastPackage=pkg;lastScreen=prefs(this).getBoolean("shorts_only",false)?"Reels/Shorts screen identified":"Watching "+(pkg.equals(IG)?"Instagram":"YouTube")+" (whole-app mode)";
        if(event.getEventType()==AccessibilityEvent.TYPE_VIEW_SCROLLED&&now-lastScroll>500){tracker.scrolled();lastScroll=now;}
    }
    private int nodesScanned;
    private boolean hasFeedId(AccessibilityNodeInfo node,int depth){
        if(depth==0)nodesScanned=0;
        if(depth>12||++nodesScanned>160)return false;String id=node.getViewIdResourceName();
        if(id!=null&&id.toLowerCase(Locale.ROOT).matches(".*(reel_recycler|reel_player|reel_watch|shorts_player|shorts_container|clips_viewer|clips_swipe|reels_viewer|reels_video|shorts_video).*"))return true;
        for(int i=0;i<Math.min(node.getChildCount(),16);i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null){boolean match=hasFeedId(child,depth+1);child.recycle();if(match)return true;}}return false;
    }
    private void save(){lastSave=SystemClock.elapsedRealtime();prefs(this).edit().putString("day",day).putLong("ig",tracker.instagram()).putLong("yt",tracker.youtube()).putInt("scrolls",tracker.scrolls()).putInt("reminders",tracker.reminders()).apply();AttentionWidget.updateAll(this);}
    private void remind(boolean test){
        if(!test&&(!enabled()||tracker.active().isEmpty()))return;
        if(!test)prefs(this).edit().putLong("last_reminder",System.currentTimeMillis()).apply();save();
        int budget=prefs(this).getInt("budget",0);boolean over=budget>0&&(tracker.instagram()+tracker.youtube())>=budget*60000L;
        String reached=goal();String title=test?"Scroll Guard test":!reached.isEmpty()||over?"Your chosen daily goal is here":"A moment to check in";
        String detail=test?"This is the reminder you’ll see in Instagram or YouTube.":!reached.isEmpty()?reached:"You’ve been here for "+Math.max(1,tracker.continuous()/60000)+" minutes. Is this still how you want to spend this moment?";
        if(prefs(this).getBoolean("overlay",true))try{showOverlay(title,detail,test);return;}catch(RuntimeException ignored){hideOverlay();}
        NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(manager==null)return;
        manager.createNotificationChannel(new NotificationChannel("scroll_guard","Automatic scrolling reminders",NotificationManager.IMPORTANCE_HIGH));
        Intent open=getPackageManager().getLaunchIntentForPackage(getPackageName());PendingIntent content=open==null?null:PendingIntent.getActivity(this,904,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=new Notification.Builder(this,"scroll_guard").setSmallIcon(R.drawable.ic_stat_attention).setContentTitle(title).setContentText(detail).setStyle(new Notification.BigTextStyle().bigText(detail)).setAutoCancel(true);if(content!=null)b.setContentIntent(content);
        try{manager.notify(904,b.build());}catch(SecurityException ignored){}
    }
    private int dp(int n){return(int)(getResources().getDisplayMetrics().density*n);}
    private void showBlock(String reason,boolean test){
        try{
            hideOverlay();boolean dark=MainActivity.isDark(this);LinearLayout screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);screen.setGravity(Gravity.CENTER);screen.setPadding(dp(28),dp(48),dp(28),dp(36));screen.setBackgroundColor(Color.parseColor(dark?"#16251F":"#F7F5ED"));
            TextView title=new TextView(this);title.setText(test?"Scrolling pause · test":"Your scrolling pause");title.setTextSize(30);title.setTypeface(Typeface.SERIF);title.setTextColor(Color.parseColor(dark?"#E4F0DF":"#233D32"));screen.addView(title);
            TextView body=new TextView(this);body.setText(reason+"\n\nThis pause covers the feed. Your daily goals reset at midnight. You can leave now, or deliberately add 5 minutes.");body.setTextColor(Color.parseColor(dark?"#B9C9B3":"#56654E"));body.setTextSize(17);body.setPadding(0,dp(24),0,dp(28));screen.addView(body);
            addAction(screen,test?"Close test pause":"Leave this app",()->{hideOverlay();if(!test){tracker.leave(SystemClock.elapsedRealtime());performGlobalAction(GLOBAL_ACTION_HOME);save();}});
            if(!test){addAction(screen,"Add 5 minutes",()->{body.setText("Add 5 minutes? Your goal remains saved. The pause returns after the extension ends.");screen.removeViews(2,screen.getChildCount()-2);addAction(screen,"Confirm 5-minute extension",()->{extensionElapsedUntil=SystemClock.elapsedRealtime()+300000;prefs(this).edit().putLong("extension_until",System.currentTimeMillis()+300000).apply();hideOverlay();});addAction(screen,"Keep my goal · leave app",()->{hideOverlay();tracker.leave(SystemClock.elapsedRealtime());performGlobalAction(GLOBAL_ACTION_HOME);save();});});}
            screen.setFocusableInTouchMode(true);screen.setOnKeyListener((v,key,event)->key==KeyEvent.KEYCODE_BACK);
            WindowManager.LayoutParams params=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,0,android.graphics.PixelFormat.OPAQUE);params.gravity=Gravity.CENTER;
            ScrollView container=new ScrollView(this);container.setFillViewport(true);container.setBackgroundColor(Color.parseColor(dark?"#16251F":"#F7F5ED"));container.addView(screen,new ScrollView.LayoutParams(-1,-2));
            overlay=container;blockingOverlay=true;testingOverlay=test;((WindowManager)getSystemService(WINDOW_SERVICE)).addView(container,params);screen.requestFocus();blockError="";save();
        }catch(RuntimeException error){hideOverlay();blockError="Android could not show the pause screen. Check Accessibility access.";if(!test)performGlobalAction(GLOBAL_ACTION_HOME);}
    }
    private void showOverlay(String title,String detail,boolean test){
        hideOverlay();boolean dark=MainActivity.isDark(this);LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(24),dp(24),dp(24),dp(18));
        android.graphics.drawable.GradientDrawable background=new android.graphics.drawable.GradientDrawable();background.setColor(Color.parseColor(dark?"#1C3027":"#F8F7ED"));background.setCornerRadius(dp(24));card.setBackground(background);card.setElevation(dp(20));
        TextView heading=new TextView(this);heading.setText(title);heading.setTextColor(Color.parseColor(dark?"#EBF4E6":"#233D32"));heading.setTextSize(24);heading.setTypeface(Typeface.SERIF);card.addView(heading);
        TextView body=new TextView(this);body.setText(detail);body.setTextColor(Color.parseColor(dark?"#B9C9B3":"#64715B"));body.setTextSize(16);body.setPadding(0,dp(14),0,dp(18));card.addView(body);
        addAction(card,test?"Close test":"Take a break",()->{hideOverlay();if(!test){tracker.leave(SystemClock.elapsedRealtime());performGlobalAction(GLOBAL_ACTION_HOME);}});
        addAction(card,"5 more minutes",()->{tracker.snooze(300);hideOverlay();});
        addAction(card,"Pause reminders for today",()->{Calendar date=Calendar.getInstance();date.add(Calendar.DAY_OF_YEAR,1);date.set(Calendar.HOUR_OF_DAY,0);date.set(Calendar.MINUTE,0);date.set(Calendar.SECOND,0);prefs(this).edit().putLong("paused_until",date.getTimeInMillis()).apply();tracker.leave(SystemClock.elapsedRealtime());hideOverlay();});
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels-dp(32),dp(430)),WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,android.graphics.PixelFormat.TRANSLUCENT);params.gravity=Gravity.CENTER;
        overlay=card;testingOverlay=test;((WindowManager)getSystemService(WINDOW_SERVICE)).addView(card,params);
    }
    private void addAction(LinearLayout parent,String label,Runnable action){Button button=new Button(this);button.setText(label);button.setAllCaps(false);button.setTextColor(Color.parseColor("#233D32"));button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CEDFC2")));button.setOnClickListener(v->action.run());parent.addView(button,new LinearLayout.LayoutParams(-1,dp(51)));}
    private void hideOverlay(){if(overlay!=null){try{((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(overlay);}catch(RuntimeException ignored){}overlay=null;}blockingOverlay=false;testingOverlay=false;}
    @Override public void onInterrupt(){tracker.leave(SystemClock.elapsedRealtime());hideOverlay();save();}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);tracker.leave(SystemClock.elapsedRealtime());save();hideOverlay();connected=false;instance=null;super.onDestroy();}
}

package com.attentiontwin.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.service.notification.*;
import android.util.Log;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Capture before removing an ordinary alert. Never snooze an entire conversation for AI review. */
public final class FocusListenerService extends NotificationListenerService {
    public static volatile boolean connected;
    public static volatile long connectedAt;
    private static volatile long lastRebindAttemptAt;
    private static volatile FocusListenerService instance;
    private Store history;
    private InboxStore inbox;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService classifier=Executors.newFixedThreadPool(2);
    private final Set<String> scheduled=new HashSet<>(),inFlight=new HashSet<>();
    private volatile boolean destroyed;
    private static final java.util.concurrent.atomic.AtomicLong generation=new java.util.concurrent.atomic.AtomicLong();
    static void invalidateInbox(){generation.incrementAndGet();}
    interface ReviewProvider{JSONObject review(Context context,JSONArray messages) throws Exception;}
    static ReviewProvider reviewer=GeminiClient::reviewBatch;

    public static void recheckAndRebind(Context context){long now=System.currentTimeMillis();if(FocusUtils.hasNotificationAccess(context)&&!connected&&now-lastRebindAttemptAt>30000){lastRebindAttemptAt=now;try{requestRebind(new ComponentName(context,FocusListenerService.class));}catch(RuntimeException ignored){}}}
    static void reviewWaiting(){FocusListenerService service=instance;if(service!=null)service.main.post(()->{if(service.destroyed)return;if(!GeminiClient.enabled(service)){service.inbox.pauseReview();return;}JSONArray groups=service.inbox.pendingGroups();for(int i=0;i<groups.length();i++)service.queue(groups.optString(i));});}
    @Override public void onCreate(){super.onCreate();history=new Store(this);inbox=new InboxStore(this);inbox.recoverReviewing();instance=this;}
    @Override public void onListenerConnected(){super.onListenerConnected();connected=true;connectedAt=System.currentTimeMillis();reviewWaiting();main.post(recoverLegacy);}
    @Override public void onListenerDisconnected(){connected=false;connectedAt=0;try{requestRebind(new ComponentName(this,FocusListenerService.class));}catch(RuntimeException ignored){}super.onListenerDisconnected();}
    @Override public void onDestroy(){destroyed=true;connected=false;instance=null;main.removeCallbacksAndMessages(null);classifier.shutdownNow();if(history!=null)history.close();if(inbox!=null)inbox.close();super.onDestroy();}
    @Override public void onNotificationPosted(StatusBarNotification notification){try{receive(notification,false);}catch(Exception error){FocusUtils.prefs(this).edit().putString("last_capture_error","Could not save/encrypt an alert. Its original was left available.").putLong("last_callback_at",System.currentTimeMillis()).apply();Log.e("AttentionTwin","Could not safely capture alert; left available",error);}}
    private final Runnable recoverLegacy=new Runnable(){public void run(){if(destroyed)return;boolean remain=false;try{StatusBarNotification[] old=getSnoozedNotifications();remain=old!=null&&old.length>0;if(old!=null)for(StatusBarNotification n:old)receive(n,true);}catch(Exception ignored){}if(remain&&System.currentTimeMillis()-connectedAt<24*3600000L)main.postDelayed(this,15000);}};

    private void receive(StatusBarNotification sbn,boolean legacy) throws Exception {
        if(sbn==null||sbn.getNotification()==null)return;Notification n=sbn.getNotification();
        if(nonActionable(n))return;
        boolean own=getPackageName().equals(sbn.getPackageName()),test=own&&n.extras!=null&&n.extras.getBoolean("at_test",false);
        if(own&&!test)return;
        String pkg=test?n.extras.getString("at_fake_pkg","com.instagram.android"):sbn.getPackageName();
        String label=label(pkg),title=n.extras==null?"":String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TITLE,""));
        String role=FocusUtils.roleFor(this,pkg),source=test?"test":"real",hash=keyHash(sbn.getKey());long now=System.currentTimeMillis();
        boolean active=FocusUtils.isFilteringActive(this),base=active&&(role.equals("SMART")||role.equals("BLOCK_IN_FOCUS")),shield=active&&FocusUtils.prefs(this).getBoolean("block_promotions",true);
        if(!active||(!base&&!shield)){if(!legacy)log(hash,pkg,label,role,source,"ALLOW","SHOWN",!active?"Filtering is off.":"App rule allows notifications.");return;}
        String words=FocusUtils.prefs(this).getString("urgent_words","");String group=keyHash(source+"\n"+pkg+"\n"+title);
        boolean bypass=false,ordinary=false;int added=0,important=0;String lastReason="No new message content.";
        for(NotificationText.Message message:NotificationText.messages(n)){
            String text=message.text,kind=NotificationRules.kind(title,text,words);boolean vip=FocusUtils.isVip(this,title,text);
            DecisionEngine.Input safety=new DecisionEngine.Input();safety.packageName=pkg;safety.notificationCategory=n.category;safety.title=title;safety.text=text;safety.urgentWords="";
            boolean safe=vip||kind.equals("EMERGENCY")||(!kind.equals("PROMOTION")&&!DecisionEngine.hardExceptionReason(safety).isEmpty());
            if(!base&&!(shield&&kind.equals("PROMOTION")))continue;
            if(vip){kind="IMPORTANT";lastReason="VIP contact: bypassed filtering and saved in Important.";}else lastReason=NotificationRules.reason(kind);
            String state=kind.equals("EMERGENCY")?"IMPORTANT":kind;
            long timestamp=message.at>0?message.at:(n.when>0?n.when:sbn.getPostTime());timestamp=Math.min(now,timestamp);
            String fingerprint=keyHash(source+"\n"+pkg+"\n"+title+"\n"+text+"\n"+timestamp);
            if(timestamp<now-7L*86400000)continue;
            long id=inbox.capture(fingerprint,group,pkg,label,timestamp,source,title,text,state,lastReason);
            if(!safe)ordinary=true;
            if(id<0)continue;added++;if(safe)bypass=true;
            if(state.equals("IMPORTANT"))important++;
            else if(state.equals("PENDING")){
                if(role.equals("SMART")&&GeminiClient.enabled(this))queue(group);
                else inbox.state(id,"UNKNOWN",role.equals("BLOCK_IN_FOCUS")?"Hold mode: saved for your review; not sent to Gemini.":"Gemini is off: saved for your review.");
            }
        }
        // Capture succeeded before removal. Leaving emergencies/OTPs visible is independent of Gemini.
        if(!legacy&&ordinary&&!bypass){try{cancelNotification(sbn.getKey());log(hash,pkg,label,role,source,"SUPPRESS","CAPTURED",added+" new messages saved before removal. "+lastReason);}catch(RuntimeException error){log(hash,pkg,label,role,source,"ALLOW","SHOWN","Saved, but Android rejected removing this alert.");}}
        else if(added>0)log(hash,pkg,label,role,source,"ALLOW",legacy?"RECOVERED":"SHOWN",legacy?"Recovered messages from an earlier Android snooze. Originals remain on Android's schedule.":"Safety exception: original alert remains available.");
        if(important>0&&!test)notifyImportant(group,label,important);
        FocusUtils.prefs(this).edit().putLong("last_callback_at",now).putString("last_capture_error","").apply();
    }
    private void queue(String group){if(destroyed||!GeminiClient.enabled(this)||scheduled.contains(group)||inFlight.contains(group))return;if(scheduled.size()+inFlight.size()>=30)return;scheduled.add(group);main.postDelayed(()->flush(group),2500);}
    private void flush(String group){scheduled.remove(group);if(destroyed||!GeminiClient.enabled(this))return;final JSONArray batch;try{batch=inbox.pending(group,12);}catch(JSONException e){return;}if(batch.length()==0)return;if(!"SMART".equals(FocusUtils.roleFor(this,batch.optJSONObject(0).optString("pkg")))){for(int i=0;i<batch.length();i++)inbox.state(batch.optJSONObject(i).optLong("id"),"UNKNOWN","Current app rule does not use Gemini.");return;}inFlight.add(group);for(int i=0;i<batch.length();i++)inbox.state(batch.optJSONObject(i).optLong("id"),"REVIEWING","Gemini batch review in progress.");
        long version=generation.get();classifier.execute(()->{JSONObject result=null;String failure="";try{if(version==generation.get()&&GeminiClient.enabled(this))result=reviewer.review(this,batch);else failure="Gemini paused.";}catch(Exception e){failure=e.getMessage()==null?"Gemini unavailable.":e.getMessage();}final JSONObject response=result;final String error=failure;main.post(()->{if(version==generation.get())finish(group,batch,response,error);else inFlight.remove(group);});});
    }
    private void finish(String group,JSONArray batch,JSONObject response,String failure){if(destroyed)return;inFlight.remove(group);int important=0;JSONArray results=response==null?new JSONArray():response.optJSONArray("items");
        for(int i=0;i<batch.length();i++){JSONObject item=batch.optJSONObject(i);long id=item.optLong("id");String state="UNKNOWN",reason="Review unavailable; saved for your review. "+failure;
            if(results!=null&&GeminiClient.enabled(this))for(int j=0;j<results.length();j++){JSONObject r=results.optJSONObject(j);if(r==null||r.optLong("id",-1)!=id)continue;String kind=r.optString("classification");double confidence=r.optDouble("confidence",-1);if(!Double.isFinite(confidence)||confidence<0||confidence>1)break;if((kind.equals("URGENT")||kind.equals("IMPORTANT"))&&confidence>=.6){state="IMPORTANT";important++;}else if(kind.equals("CASUAL")||kind.equals("ROUTINE"))state="CASUAL";reason="Gemini batch · "+kind+" ("+Math.round(confidence*100)+"%). Saved in "+(state.equals("IMPORTANT")?"Important":"inbox")+".";break;}
            inbox.state(id,state,reason);
        }
        if(response!=null&&GeminiClient.enabled(this))try{String summary=response.optString("summary");if(!summary.isEmpty())inbox.summary(group,summary.substring(0,Math.min(summary.length(),500)),batch.length());}catch(Exception ignored){}
        JSONObject first=batch.optJSONObject(0);if(important>0&&first!=null&&!first.optString("source").equals("test"))notifyImportant(group,first.optString("label","App"),important);
        reviewWaiting();
    }
    private void notifyImportant(String group,String label,int count){NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(manager==null)return;manager.createNotificationChannel(new NotificationChannel("important_inbox_v5","Important captured messages",NotificationManager.IMPORTANCE_HIGH));Intent open=new Intent(this,MainActivity.class).putExtra("at_page","messages").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);PendingIntent intent=PendingIntent.getActivity(this,7000+Math.abs(group.hashCode()%10000),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);try{manager.notify("important-inbox",Math.abs(group.hashCode()),new Notification.Builder(this,"important_inbox_v5").setSmallIcon(R.drawable.ic_stat_attention).setContentTitle("Important · "+label).setContentText("Open AttentionTwin to review important messages.").setContentIntent(intent).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).setGroup("attentiontwin.important").build());}catch(SecurityException ignored){}}
    private void log(String hash,String pkg,String label,String role,String source,String action,String state,String reason){Store.Event event=new Store.Event();event.nkey=hash;event.pkg=pkg;event.label=label;event.category=FocusUtils.categoryFor(pkg,label);event.role=role;event.ts=System.currentTimeMillis();event.focus=FocusUtils.isFilteringActive(this);event.action=action;event.state=state;event.reason=reason;event.source=source;try{history.insert(event);}catch(RuntimeException ignored){}}
    @Override public void onNotificationRemoved(StatusBarNotification sbn,RankingMap map,int reason){if(sbn!=null&&history!=null&&reason==REASON_CLICK)history.markOpenedByKey(keyHash(sbn.getKey()));}
    private boolean nonActionable(Notification n){return (n.flags&(Notification.FLAG_ONGOING_EVENT|Notification.FLAG_NO_CLEAR))!=0||Notification.CATEGORY_SERVICE.equals(n.category)||Notification.CATEGORY_TRANSPORT.equals(n.category)||Notification.CATEGORY_CALL.equals(n.category)||Notification.CATEGORY_ALARM.equals(n.category)||Notification.CATEGORY_NAVIGATION.equals(n.category);}
    private String label(String pkg){try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception e){return pkg;}}
    static String keyHash(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(int i=0;i<16;i++)out.append(String.format(Locale.ROOT,"%02x",bytes[i]));return out.toString();}catch(Exception e){return Integer.toHexString(value.hashCode());}}
}

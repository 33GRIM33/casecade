package com.attentiontwin.app;
import android.app.*;
import android.content.*;
import android.os.*;
import android.service.notification.*;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNotificationListenerService;
import org.json.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

/** Runs the real pipeline; captures OS removal/snoozing instead of pretending a phone executed it. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,shadows=NotificationFlowTest.CapturingListener.class) @LooperMode(LooperMode.Mode.PAUSED)
public class NotificationFlowTest {
 @Implements(NotificationListenerService.class) public static class CapturingListener extends ShadowNotificationListenerService {
  public int removed,snoozed;
  @Implementation protected void cancelNotification(String key){removed++;super.cancelNotification(key);}
  @Implementation protected void snoozeNotification(String key,long millis){snoozed++;}
 }
 private Context context;private ServiceController<FocusListenerService> controller;private FocusListenerService listener;private CapturingListener shadow;private long clock;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();InboxCrypto.keys=()->new SecretKeySpec(new byte[16],"AES");context.getSharedPreferences("gemini_private",0).edit().clear().apply();FocusUtils.prefs(context).edit().clear().putString("default_role","SMART").putString("filter_scope","always").apply();controller=Robolectric.buildService(FocusListenerService.class).create();listener=controller.get();shadow=Shadow.extract(listener);clock=System.currentTimeMillis()-10000;NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);manager.createNotificationChannel(new NotificationChannel("message","messages",3));}
 @After public void cleanup(){controller.destroy();InboxCrypto.keys=InboxCrypto::androidKey;FocusListenerService.reviewer=GeminiClient::reviewBatch;}
 private StatusBarNotification post(String title,String text){return post(new Notification.Builder(context,"message").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(text).setCategory(Notification.CATEGORY_MESSAGE).setWhen(++clock).build());}
 private StatusBarNotification post(Notification n){StatusBarNotification sbn=new StatusBarNotification("com.whatsapp","com.whatsapp",42,null,10001,0,0,n,android.os.Process.myUserHandle(),n.when);shadow.addActiveNotification(sbn);listener.onNotificationPosted(sbn);Shadows.shadowOf(Looper.getMainLooper()).idle();return sbn;}
 private JSONArray saved() throws Exception {try(InboxStore store=new InboxStore(context)){return store.messages(false);}}
 @Test public void greetingSavedBeforeRemovalAndNeverConversationSnoozed() throws Exception {post("Sam","hello");assertEquals(1,shadow.removed);assertEquals(0,shadow.snoozed);assertEquals(1,saved().length());assertEquals("CASUAL",saved().getJSONObject(0).getString("state"));}
 @Test public void urgentFireBypassesEvenWithNoCustomUrgentWordsAndInHoldMode() throws Exception {FocusUtils.prefs(context).edit().putString("default_role","BLOCK_IN_FOCUS").putString("urgent_words","").apply();post("Sam","there is a urgent fire");assertEquals(0,shadow.removed);assertEquals("IMPORTANT",saved().getJSONObject(0).getString("state"));}
 @Test public void vipAndVerificationMessagesBypass() throws Exception {FocusUtils.prefs(context).edit().putString("vip_contacts","Mom").apply();post("Mom","hello");post("Bank","Your verification code is 123456");assertEquals(0,shadow.removed);assertEquals(2,saved().length());}
 @Test public void oldReleasePassDoesNotAllowNewPromotionOrGreeting() throws Exception {StatusBarNotification first=post("Sam","hello");FocusUtils.prefs(context).edit().putBoolean("snooze_release_once_"+FocusListenerService.keyHash(first.getKey()),true).apply();post("Sam","hello hello");assertEquals(2,shadow.removed);assertEquals(2,saved().length());}
 @Test public void promotionShieldRunsBeforeAllowAppAndGenericUrgentWord() throws Exception {FocusUtils.prefs(context).edit().putString("default_role","ALLOW").putString("urgent_words","urgent").putBoolean("block_promotions",true).apply();post("Shop","URGENT flash sale 70% off, shop now");assertEquals(1,shadow.removed);assertEquals("PROMOTION",saved().getJSONObject(0).getString("state"));}
 @Test public void promotionBodyCannotTriggerVipBySubstring() throws Exception {FocusUtils.prefs(context).edit().putString("vip_contacts","Mom").apply();post("Shop","Your moment to save: special offer 50% off for Mom");assertEquals(1,shadow.removed);assertEquals("PROMOTION",saved().getJSONObject(0).getString("state"));}
 @Test public void messagingHistoryPreservesEveryNewMessageWithoutDuplicates() throws Exception {Person sender=new Person.Builder().setName("Sam").build();NotificationCompat.MessagingStyle style=new NotificationCompat.MessagingStyle(new Person.Builder().setName("Me").build());style.addMessage("hello",++clock,sender);style.addMessage("there is a urgent fire",++clock,sender);style.addMessage("hello hello",++clock,sender);Notification n=new NotificationCompat.Builder(context,"message").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Sam").setStyle(style).setWhen(clock).build();post(n);assertEquals(3,saved().length());assertEquals(0,shadow.removed);post(n);assertEquals(3,saved().length());}
 @Test public void burstIsCapturedBeforeOneBatchAndImportantResultStaysInApp() throws Exception {
  context.getSharedPreferences("gemini_private",0).edit().putString("key_cipher","test-only").putBoolean("enabled",true).apply();AtomicInteger calls=new AtomicInteger();
  FocusListenerService.reviewer=(c,batch)->{calls.incrementAndGet();JSONArray items=new JSONArray();for(int i=0;i<batch.length();i++)items.put(new JSONObject().put("id",batch.getJSONObject(i).getLong("id")).put("classification",i==0?"IMPORTANT":"CASUAL").put("confidence",.9));return new JSONObject().put("items",items).put("summary","One practical request and casual discussion.");};
  for(int i=0;i<8;i++)post("Sam","Please bring document number "+i+" when you return.");assertEquals(8,shadow.removed);assertEquals(8,saved().length());assertEquals(0,calls.get());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(3));
  long until=System.nanoTime()+3000000000L;boolean done=false;while(System.nanoTime()<until){Shadows.shadowOf(Looper.getMainLooper()).idle();JSONArray rows=saved();for(int i=0;i<rows.length();i++)if(rows.getJSONObject(i).getString("state").equals("IMPORTANT"))done=true;if(done)break;Thread.sleep(10);}
  assertTrue(done);assertEquals(1,calls.get());try(InboxStore store=new InboxStore(context)){assertEquals(1,store.summaries().length());}
 }
 @Test public void batchFailureDoesNotLoseCapturedMessages() throws Exception {context.getSharedPreferences("gemini_private",0).edit().putString("key_cipher","test-only").putBoolean("enabled",true).apply();FocusListenerService.reviewer=(c,b)->{throw new Exception("Offline");};post("Sam","Bring the blue folder when you return");Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(3));long until=System.nanoTime()+3000000000L;while(System.nanoTime()<until){Shadows.shadowOf(Looper.getMainLooper()).idle();if(saved().getJSONObject(0).getString("state").equals("UNKNOWN"))break;Thread.sleep(10);}assertEquals(1,saved().length());assertEquals("UNKNOWN",saved().getJSONObject(0).getString("state"));}
 @Test public void moreThanOneBatchDrainsEveryMessage() throws Exception {
  context.getSharedPreferences("gemini_private",0).edit().putString("key_cipher","test-only").putBoolean("enabled",true).apply();AtomicInteger calls=new AtomicInteger();
  FocusListenerService.reviewer=(c,b)->{calls.incrementAndGet();assertTrue(b.length()<=12);JSONArray items=new JSONArray();for(int i=0;i<b.length();i++)items.put(new JSONObject().put("id",b.getJSONObject(i).getLong("id")).put("classification","CASUAL").put("confidence",.95));return new JSONObject().put("items",items);};
  for(int i=0;i<14;i++)post("Sam","Bring the blue folder number "+i+" when you return");assertEquals(14,saved().length());
  long until=System.nanoTime()+5000000000L;int reviewed=0;while(System.nanoTime()<until){Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(3));reviewed=0;JSONArray rows=saved();for(int i=0;i<rows.length();i++)if("CASUAL".equals(rows.getJSONObject(i).getString("state")))reviewed++;if(reviewed==14)break;Thread.sleep(10);}assertEquals(14,reviewed);assertEquals(2,calls.get());
 }
 @Test public void expiredContentIsNeverRemovedWithoutCapture() throws Exception {
  Notification n=new Notification.Builder(context,"message").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Sam").setContentText("hello").setWhen(System.currentTimeMillis()-8L*86400000).build();post(n);assertEquals(0,shadow.removed);assertEquals(0,saved().length());
 }

}

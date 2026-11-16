package com.attentiontwin.app;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.util.ReflectionHelpers;
import java.time.Duration;
import static org.junit.Assert.*;

/** Android framework simulations: these are not claims about a physical phone. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28) @LooperMode(LooperMode.Mode.PAUSED)
public class AndroidWellnessTest {
 private Context context;private ServiceController<ScrollGuardService> controller;private ScrollGuardService service;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();FocusUtils.prefs(context).edit().clear().apply();ScrollGuardService.prefs(context).edit().clear().apply();}
 @After public void cleanup(){if(controller!=null)controller.destroy();}
 @Test public void helloIsHeldWithGeminiDisabledAndWithUnreadableKey(){
  assertTrue(GeminiClient.decide(context,"WhatsApp","Alex","hello").defer);
  context.getSharedPreferences("gemini_private",0).edit().putString("key_cipher","invalid").putBoolean("enabled",true).apply();
  assertTrue(GeminiClient.decide(context,"WhatsApp","Alex","hello").defer);
  assertTrue(GeminiClient.decide(context,"WhatsApp","Alex","hello").reason.contains("Local Smart"));
 }
 @Test public void latestWhatsAppMessageDoesNotInheritOldUrgency(){
  NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);manager.createNotificationChannel(new NotificationChannel("test","test",3));
  NotificationCompat.MessagingStyle style=new NotificationCompat.MessagingStyle(new Person.Builder().setName("Me").build());
  Person sender=new Person.Builder().setName("Alex").build();style.addMessage("Urgent doctor update",1,sender);style.addMessage("hello",2,sender);
  Notification notification=new NotificationCompat.Builder(context,"test").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Alex").setStyle(style).build();
  String body=NotificationText.body(notification);assertTrue(body.contains("hello"));assertFalse(body.contains("doctor"));assertTrue(GeminiPolicy.casualLocally(body));
  DecisionEngine.Input input=new DecisionEngine.Input();input.text=body;input.urgentWords="urgent,doctor";assertEquals("",DecisionEngine.hardExceptionReason(input));
 }
 @Test public void defaultRoleCoversNewAppsButImportantOverrideStays(){FocusUtils.prefs(context).edit().putString("default_role","SMART").putString("role_com.whatsapp","IMPORTANT").apply();assertEquals("SMART",FocusUtils.roleFor(context,"com.new.app"));assertEquals("IMPORTANT",FocusUtils.roleFor(context,"com.whatsapp"));}
 @Test public void accessibilityGrantAcceptsAndroidShortComponentNames(){android.provider.Settings.Secure.putString(context.getContentResolver(),android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,"com.other/.Service:com.attentiontwin.app/.ScrollGuardService");assertTrue(ScrollGuardService.accessGranted(context));android.provider.Settings.Secure.putString(context.getContentResolver(),android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,"com.attentiontwin.app.extra/.ScrollGuardService");assertFalse(ScrollGuardService.accessGranted(context));}
 @Test public void reachedQuotaSurvivesRestartWithoutLaunchingPermissionScreen() throws Exception {start(1);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(61));assertTrue(ScrollGuardService.state(context).getBoolean("blockingVisible"));controller.destroy();controller=Robolectric.buildService(ScrollGuardService.class).create();service=controller.get();service.onServiceConnected();android.view.accessibility.AccessibilityNodeInfo root=android.view.accessibility.AccessibilityNodeInfo.obtain();root.setPackageName("com.instagram.android");org.robolectric.shadows.ShadowAccessibilityService shadow=org.robolectric.shadow.api.Shadow.extract(service);shadow.setRootInActiveWindow(root);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3));assertTrue(ScrollGuardService.state(context).getBoolean("blockingVisible"));assertTrue(ScrollGuardService.state(context).getBoolean("consent"));assertNull(Shadows.shadowOf((android.app.Application)context).getNextStartedActivity());}
 @Test public void bulkRulesResetOverridesPreserveImportantAndResume(){FocusUtils.prefs(context).edit().putBoolean("paused",true).putString("role_com.whatsapp","IMPORTANT").putString("role_com.instagram.android","BLOCK_IN_FOCUS").apply();assertEquals(1,FocusUtils.applyAllRoles(context,"SMART",true,true));assertEquals("IMPORTANT",FocusUtils.roleFor(context,"com.whatsapp"));assertEquals("SMART",FocusUtils.roleFor(context,"com.instagram.android"));assertEquals("SMART",FocusUtils.roleFor(context,"com.new.app"));assertTrue(FocusUtils.isFilteringActive(context));FocusUtils.applyAllRoles(context,"BLOCK_IN_FOCUS",false,true);assertEquals("BLOCK_IN_FOCUS",FocusUtils.roleFor(context,"com.whatsapp"));}
 private void start(int limit){ScrollGuardService.prefs(context).edit().putBoolean("enabled",true).putBoolean("consent",true).putInt("ig_limit",limit).putString("limit_mode","block").putInt("interval",3600).apply();controller=Robolectric.buildService(ScrollGuardService.class).create();service=controller.get();service.onServiceConnected();Shadows.shadowOf(Looper.getMainLooper()).idle();AccessibilityEvent event=AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);event.setPackageName("com.instagram.android");service.onAccessibilityEvent(event);}
 @Test public void dailyGoalDisplaysFullScreenPauseAndLeavingClearsIt() throws Exception {
  start(1);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(61));
  assertTrue(ScrollGuardService.state(context).getBoolean("blockingVisible"));View overlay=ReflectionHelpers.getField(service,"overlay");assertNotNull(overlay);assertEquals(-1,overlay.getLayoutParams().height);assertEquals(-1,overlay.getLayoutParams().width);
  long used=ScrollGuardService.state(context).getLong("instagramSeconds");Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(10));assertEquals(used,ScrollGuardService.state(context).getLong("instagramSeconds"));
  Button leave=findButton((ViewGroup)overlay,"Leave this app");assertNotNull(leave);leave.performClick();assertFalse(ScrollGuardService.state(context).getBoolean("blockingVisible"));
 }
 @Test public void homeWidgetRendersAndItsThemeToggleUpdatesIt(){
  android.appwidget.AppWidgetManager manager=android.appwidget.AppWidgetManager.getInstance(context);org.robolectric.shadows.ShadowAppWidgetManager shadow=Shadows.shadowOf(manager);
  int id=shadow.createWidgets(AttentionWidget.class,R.layout.attention_widget,1)[0];View view=shadow.getViewFor(id);assertEquals("AttentionTwin",((TextView)view.findViewById(R.id.widget_title)).getText().toString());assertEquals("Light widget",((TextView)view.findViewById(R.id.widget_theme)).getText().toString());
  new AttentionWidget().onReceive(context,new Intent(context,AttentionWidget.class).setAction("com.attentiontwin.WIDGET_THEME"));view=shadow.getViewFor(id);assertEquals("Dark widget",((TextView)view.findViewById(R.id.widget_theme)).getText().toString());
 }
 @Test public void confirmedExtensionClearsPauseAndReblocksAfterExpiry() throws Exception {
  start(1);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(61));ViewGroup overlay=ReflectionHelpers.getField(service,"overlay");findButton(overlay,"Add 5 minutes").performClick();assertTrue(ScrollGuardService.state(context).getBoolean("blockingVisible"));findButton(overlay,"Confirm 5-minute extension").performClick();assertFalse(ScrollGuardService.state(context).getBoolean("blockingVisible"));assertTrue(ScrollGuardService.prefs(context).getLong("extension_until",0)>System.currentTimeMillis());
  Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(301));assertTrue("now="+System.currentTimeMillis()+" prefs="+ScrollGuardService.prefs(context).getAll()+" state="+ScrollGuardService.state(context),ScrollGuardService.state(context).getBoolean("blockingVisible"));
 }
 private Button findButton(ViewGroup group,String text){for(int i=0;i<group.getChildCount();i++){View v=group.getChildAt(i);if(v instanceof Button&&((Button)v).getText().toString().equals(text))return (Button)v;if(v instanceof ViewGroup){Button found=findButton((ViewGroup)v,text);if(found!=null)return found;}}return null;}
}

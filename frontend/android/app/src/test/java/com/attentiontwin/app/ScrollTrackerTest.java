package com.attentiontwin.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScrollTrackerTest {
 private static final String IG="com.instagram.android",YT="com.google.android.youtube";
 private void advance(ScrollTracker t,long start,int seconds){for(int i=1;i<=seconds;i++)t.tick(start+i*1000,true);}
 @Test public void tenSecondReminderAndRepeat(){ScrollTracker t=new ScrollTracker();t.configure(10);t.enter(IG,0);advance(t,0,9);assertFalse(t.due());t.tick(10000,true);assertTrue(t.due());t.reminded();assertEquals(1,t.reminders());assertEquals(10000,t.nextIn());advance(t,10000,10);assertTrue(t.due());}
 @Test public void timeOnlyCountsInWatchedForeground(){ScrollTracker t=new ScrollTracker();t.enter(IG,0);advance(t,0,10);t.leave(10000);advance(t,10000,20);assertEquals(10000,t.instagram());assertFalse(t.due());t.enter(YT,30000);advance(t,30000,5);assertEquals(5000,t.youtube());assertEquals(15000,t.continuous());}
 @Test public void thirtySecondsAwayResetsSession(){ScrollTracker t=new ScrollTracker();t.configure(10);t.enter(IG,0);advance(t,0,8);t.leave(8000);t.enter(YT,38000);assertEquals(0,t.continuous());assertEquals(10000,t.nextIn());assertEquals(8000,t.instagram());}
 @Test public void briefInterruptionPreservesSession(){ScrollTracker t=new ScrollTracker();t.enter(IG,0);advance(t,0,8);t.leave(8000);t.enter(IG,20000);assertEquals(8000,t.continuous());}
 @Test public void screenOffAndClockJumpDoNotInflateUsage(){ScrollTracker t=new ScrollTracker();t.enter(IG,0);t.tick(1000,false);assertEquals(0,t.instagram());t.tick(100000,true);assertEquals(5000,t.instagram());t.tick(90000,true);assertEquals(5000,t.instagram());}
 @Test public void snoozeAndIntervalChange(){ScrollTracker t=new ScrollTracker();t.enter(IG,0);advance(t,0,10);t.snooze(300);assertEquals(300000,t.nextIn());t.configure(60);assertEquals(60000,t.nextIn());}
 @Test public void dailyTotalsRestoreAndReset(){ScrollTracker t=new ScrollTracker();t.restore(1200,3000,5,2);assertEquals(1200,t.instagram());assertEquals(3000,t.youtube());assertEquals(5,t.scrolls());assertEquals(2,t.reminders());t.scrolled();assertEquals(5,t.scrolls());t.enter(IG,0);t.scrolled();assertEquals(6,t.scrolls());t.resetDay();assertEquals(0,t.instagram());assertEquals(0,t.youtube());assertEquals(0,t.reminders());}
}

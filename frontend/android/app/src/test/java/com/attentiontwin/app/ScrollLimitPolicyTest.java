package com.attentiontwin.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScrollLimitPolicyTest {
 private final String IG="com.instagram.android",YT="com.google.android.youtube";
 @Test public void noGoalMeansNoBlock(){assertEquals("",ScrollLimitPolicy.reached(IG,900000,900000,0,0,0,0,1000));}
 @Test public void blocksAtTheExactInstagramGoal(){assertEquals("",ScrollLimitPolicy.reached(IG,59999,0,0,1,0,0,1000));assertTrue(ScrollLimitPolicy.reached(IG,60000,0,0,1,0,0,1000).contains("Instagram"));}
 @Test public void appGoalsAreIndependent(){assertEquals("",ScrollLimitPolicy.reached(YT,900000,0,0,1,10,0,1000));assertTrue(ScrollLimitPolicy.reached(YT,0,60000,0,0,1,0,1000).contains("YouTube"));}
 @Test public void combinedGoalProtectsEitherApp(){assertTrue(ScrollLimitPolicy.reached(IG,30000,30000,1,0,0,0,1000).contains("combined"));assertTrue(ScrollLimitPolicy.reached(YT,30000,30000,1,0,0,0,1000).contains("combined"));}
 @Test public void extensionExpiresAndGoalStillApplies(){assertEquals("",ScrollLimitPolicy.reached(IG,60000,0,0,1,0,5000,1000));assertFalse(ScrollLimitPolicy.reached(IG,60000,0,0,1,0,5000,5000).isEmpty());}
 @Test public void outsideWatchedAppNeverBlocks(){assertEquals("",ScrollLimitPolicy.reached("",60000,60000,1,1,1,0,1000));}
 @Test public void midnightResetRemovesReachedGoal(){assertFalse(ScrollLimitPolicy.reached(IG,60000,0,0,1,0,0,1000).isEmpty());assertEquals("",ScrollLimitPolicy.reached(IG,0,0,0,1,0,0,1000));}
}

package com.attentiontwin.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class GeminiPolicyTest {
 @Test public void greetingsAreLocalAndDoNotNeedGemini(){for(String text:new String[]{"hello","Hello!!!","hi","hey bro","Alex: hello 👋","good morning","kaise ho","namaste"})assertTrue(text,GeminiPolicy.casualLocally(text));}
 @Test public void localGreetingRuleDoesNotSwallowImportantMessages(){for(String text:new String[]{"hello please call the doctor now","hey I need help","Your verification code is 123456","hello deadline in five minutes","hello can you pick me up now?","hospital appointment changed"})assertFalse(text,GeminiPolicy.casualLocally(text));}
 @Test public void newestMessageDecidesGreetingRule(){assertTrue(GeminiPolicy.casualLocally("urgent old text\nAlex: hello"));assertFalse(GeminiPolicy.casualLocally("Alex: hello\nAlex: emergency please call now"));}
 @Test public void casualChatWaits(){assertTrue(GeminiPolicy.shouldDefer("CASUAL",.82,"focus_first"));assertTrue(GeminiPolicy.shouldDefer("CASUAL",.82,"balanced"));}
 @Test public void focusFirstHoldsUnclearContent(){assertTrue(GeminiPolicy.shouldDefer("UNCERTAIN",.3,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("UNCERTAIN",.9,"balanced"));}
 @Test public void urgentAlwaysPasses(){assertFalse(GeminiPolicy.shouldDefer("URGENT",.1,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("IMPORTANT",.9,"focus_first"));}
 @Test public void lowConfidenceFollowsChosenStyle(){assertTrue(GeminiPolicy.shouldDefer("IMPORTANT",.3,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("IMPORTANT",.3,"balanced"));assertTrue(GeminiPolicy.shouldDefer("CASUAL",.3,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("CASUAL",.3,"balanced"));}
 @Test public void invalidNewOutputsFailOpen(){assertFalse(GeminiPolicy.shouldDefer("CASUAL",Double.NaN,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("ROUTINE",-1,"focus_first"));assertFalse(GeminiPolicy.shouldDefer("made-up",1,"focus_first"));}
 @Test public void importantNeverDeferred(){assertFalse(GeminiPolicy.shouldDefer("IMPORTANT",1));}
 @Test public void uncertaintyStaysAvailable(){assertFalse(GeminiPolicy.shouldDefer("UNCERTAIN",1));assertFalse(GeminiPolicy.shouldDefer("ROUTINE",.89));}
 @Test public void onlyConfidentRoutineMayWait(){assertTrue(GeminiPolicy.shouldDefer("ROUTINE",.95));}
 @Test public void invalidConfidenceFailsOpen(){assertFalse(GeminiPolicy.shouldDefer("ROUTINE",Double.NaN));assertFalse(GeminiPolicy.shouldDefer("ROUTINE",2));assertFalse(GeminiPolicy.shouldDefer("ROUTINE",-1));}
 @Test public void malformedLabelsFailOpen(){assertFalse(GeminiPolicy.shouldDefer("",1));assertFalse(GeminiPolicy.shouldDefer("routine",1));}
 @Test public void fallbackOnlyRecognizesObviousRoutine(){assertTrue(GeminiPolicy.routineLocally("A new video was posted"));assertTrue(GeminiPolicy.routineLocally("Flash sale today"));assertFalse(GeminiPolicy.routineLocally("Can you pick me up?"));assertFalse(GeminiPolicy.routineLocally("Payment due tomorrow"));}
}

package com.attentiontwin.app;
import org.junit.Test;import static org.junit.Assert.*;
public class NotificationRulesTest {
 @Test public void exactReportedFireDoesNotDependOnUserKeywords(){assertEquals("EMERGENCY",NotificationRules.kind("Friend","there is a urgent fire",""));assertEquals("EMERGENCY",NotificationRules.kind("Friend","there is a fire in the building",""));}
 @Test public void promotionsBeatGenericUrgentAndImportantWords(){for(String text:new String[]{"URGENT flash sale 70% off, shop now","Important exclusive offer get 20% off","Claim cashback offer now","Use promo code 123456","Fire sale ends soon"})assertEquals(text,"PROMOTION",NotificationRules.kind("Store",text,"urgent,important"));}
 @Test public void financesAndPracticalAlertsAreNotPromotions(){assertFalse(NotificationRules.promotion("Your payment was received"));assertEquals("IMPORTANT",NotificationRules.kind("School","Exam deadline today",""));assertEquals("IMPORTANT",NotificationRules.kind("Bank","Your verification code is 123456",""));}
 @Test public void repeatedAndTypoGreetingsAreLocal(){for(String text:new String[]{"hello hello","helo hello","heelo","hello 3.0","how aare u","Hey let's play some games"})assertEquals(text,"CASUAL",NotificationRules.kind("Friend",text,""));}
}

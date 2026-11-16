package com.attentiontwin.app;
import java.util.Locale;

/** Fast local rules run before cloud classification, including explicit danger and promotion checks. */
final class NotificationRules {
    static String normal(String value){return value==null?"":value.toLowerCase(Locale.ROOT).replace('’','\'').replaceAll("\\s+"," ").trim();}
    static boolean emergency(String value){String s=normal(value);if(s.matches(".*\\b(fire sale|fire drill|fire training)\\b.*")&&!s.contains("not a drill"))return false;
        return s.matches(".*\\b(there is|there's|urgent|emergency|house|building|kitchen|room|help).{0,45}\\b(fire|gas leak|smoke|accident|bleeding|ambulance|danger)\\b.*")
            ||s.matches(".*\\b(on fire|cannot breathe|can't breathe|call (112|911|999|ambulance|police)|medical emergency|help me now|i need help now)\\b.*");}
    static boolean promotion(String value){String s=normal(value);return s.matches(".*\\b(flash sale|fire sale|limited[- ]time offer|special offer|exclusive offer|sale ends|shop now|buy now|coupon|promo code|discount|clearance|deal of the day|mega sale|price drop|unsubscribe|spin (and|to) win|claim.{0,20}(reward|cashback|offer)|earn.{0,20}cashback|get.{0,20}cashback)\\b.*")
        ||s.matches(".*(\\b(save|flat|extra|upto|up to)\\b.{0,18}\\d{1,3}\\s*%|\\d{1,3}\\s*%\\s*(off|discount)|buy\\s+\\d+.{0,20}get\\s+\\d+).* ".trim());}
    static boolean casual(String text){if(GeminiPolicy.casualLocally(text))return true;String s=normal(text).replaceFirst("^[^:]{1,60}:\\s*","").replaceAll("[^\\p{L}\\p{N}\\s]"," ").replaceAll("\\s+"," ").trim();return s.matches("((hello+|helo+|heelo+|hi+|hey+|hlo|yo)\\s*){1,6}(bro|bhai|there|guys|all|yaar|[0-9 ]+)?")||s.matches("how a+re (you|u)")||s.matches("(hey )?(let s|lets|let us) play( some)? (games?|coc|pubg|bgmi)");}
    static String kind(String title,String text,String words){String content=normal(title+" "+text);if(emergency(content))return "EMERGENCY";if(promotion(content))return "PROMOTION";
        DecisionEngine.Input input=new DecisionEngine.Input();input.title=title;input.text=text;input.urgentWords="";if(!DecisionEngine.hardExceptionReason(input).isEmpty())return "IMPORTANT";
        for(String word:(words==null?"":words).split(",")){String w=normal(word);if(w.length()>1&&java.util.regex.Pattern.compile("(?<![\\p{L}\\p{N}])"+java.util.regex.Pattern.quote(w)+"(?![\\p{L}\\p{N}])").matcher(content).find())return "IMPORTANT";}
        if(content.matches(".*\\b(deadline|appointment|exam change|meeting moved|delivery needs|important)\\b.*"))return "IMPORTANT";
        if(casual(text)||GeminiPolicy.routineLocally(content))return "CASUAL";return "PENDING";
    }
    static String reason(String kind){switch(kind){case "EMERGENCY":return "Local safety rule: immediate danger; bypassed filtering.";case "IMPORTANT":return "Local importance rule: available in Important inbox.";case "PROMOTION":return "Local promotion rule: saved quietly; no Gemini request.";case "CASUAL":return "Local casual rule: saved quietly; no Gemini request.";default:return "Saved first. Waiting for optional batch review.";}}
}

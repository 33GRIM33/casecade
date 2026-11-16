package com.attentiontwin.app;

/** User-selected routing policy; a transport failure is handled separately and allowed. */
final class GeminiPolicy {
    static boolean casualLocally(String text) {
        if(text==null)return false;
        String latest=text.trim();int line=latest.lastIndexOf('\n');if(line>=0)latest=latest.substring(line+1);
        latest=latest.replaceFirst("^[^:\\n]{1,60}:\\s*","").toLowerCase(java.util.Locale.ROOT).replaceAll("[^\\p{L}\\p{N}\\s]"," ").replaceAll("\\s+"," ").trim();
        return latest.matches("(hi+|hello+|hey+|hii+|hlo|yo|sup|namaste|good morning|good evening|good night|how are you|how r u|what s up|whats up|kya haal hai|kaise ho)( (bro|bhai|there|dude|everyone|guys|all|yaar))?");
    }
    static boolean shouldDefer(String classification, double confidence) {
        return "ROUTINE".equals(classification) && Double.isFinite(confidence) && confidence >= 0.90 && confidence <= 1.0;
    }
    static boolean shouldDefer(String classification,double confidence,String policy) {
        if(!Double.isFinite(confidence)||confidence<0||confidence>1)return false;
        if("URGENT".equals(classification))return false;
        if("IMPORTANT".equals(classification))return confidence<0.75 && "focus_first".equals(policy);
        if("CASUAL".equals(classification)||"ROUTINE".equals(classification))return confidence>=0.60||"focus_first".equals(policy);
        return "UNCERTAIN".equals(classification)&&"focus_first".equals(policy);
    }
    static boolean routineLocally(String text) {
        String value = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        if(value.matches("(?s).*\\b(payment|deadline|due|security|account|help|call|meeting|appointment|exam|urgent|emergency)\\b.*"))return false;
        return value.matches("(?s).*\\b(new video|recommended for you|trending now|flash sale|discount|promotion|liked your post|new follower)\\b.*");
    }
    private GeminiPolicy() { }
}

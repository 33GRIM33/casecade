package com.attentiontwin.app;

/** Local goal enforcement, separate from reminder cadence and screen detection. */
final class ScrollLimitPolicy {
    static String reached(String pkg,long instagramMs,long youtubeMs,int combinedMinutes,int instagramMinutes,int youtubeMinutes,long extensionUntil,long now){
        if(pkg==null||pkg.isEmpty()||extensionUntil>now)return "";
        if(combinedMinutes>0&&instagramMs+youtubeMs>=combinedMinutes*60000L)return "Your combined daily scrolling goal is reached.";
        if(pkg.equals("com.instagram.android")&&instagramMinutes>0&&instagramMs>=instagramMinutes*60000L)return "Your Instagram daily goal is reached.";
        if(pkg.equals("com.google.android.youtube")&&youtubeMinutes>0&&youtubeMs>=youtubeMinutes*60000L)return "Your YouTube daily goal is reached.";
        return "";
    }
}

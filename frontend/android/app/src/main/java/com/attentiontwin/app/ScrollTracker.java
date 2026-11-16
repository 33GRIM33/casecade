package com.attentiontwin.app;

/** No Android dependencies: monotonic foreground accounting and reminder scheduling. */
final class ScrollTracker {
    private String active="";
    private long last=-1,awaySince=-1,continuous=0,instagram=0,youtube=0,next=300000;
    private long interval=300000;
    private int scrolls=0,reminders=0;
    synchronized void configure(int seconds){long value=Math.max(10,Math.min(3600,seconds))*1000L;if(interval!=value){interval=value;next=continuous+interval;}}
    synchronized void enter(String pkg,long now){tick(now,true);if(awaySince>=0&&now-awaySince>=30000){continuous=0;next=interval;}active=pkg;awaySince=-1;last=now;}
    synchronized void leave(long now){tick(now,true);if(!active.isEmpty())awaySince=now;active="";last=now;}
    synchronized void tick(long now,boolean interactive){
        if(last<0){last=now;return;}long delta=Math.max(0,Math.min(5000,now-last));last=now;
        if(!interactive||active.isEmpty())return;continuous+=delta;
        if(active.equals("com.instagram.android"))instagram+=delta;else if(active.equals("com.google.android.youtube"))youtube+=delta;
    }
    synchronized boolean due(){return !active.isEmpty()&&continuous>=next;}
    synchronized void reminded(){reminders++;next=continuous+interval;}
    synchronized void snooze(int seconds){next=continuous+Math.max(10,seconds)*1000L;}
    synchronized void scrolled(){if(!active.isEmpty())scrolls++;}
    synchronized String active(){return active;}
    synchronized long continuous(){return continuous;}
    synchronized long instagram(){return instagram;}
    synchronized long youtube(){return youtube;}
    synchronized long nextIn(){return Math.max(0,next-continuous);}
    synchronized int scrolls(){return scrolls;}
    synchronized int reminders(){return reminders;}
    synchronized void restore(long ig,long yt,int count,int reminderCount){instagram=Math.max(0,ig);youtube=Math.max(0,yt);scrolls=Math.max(0,count);reminders=Math.max(0,reminderCount);}
    synchronized void resetDay(){instagram=0;youtube=0;scrolls=0;reminders=0;}
}

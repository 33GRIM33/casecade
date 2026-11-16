package com.attentiontwin.app;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import org.json.*;

/** Encrypted, bounded seven-day inbox. Each distinct message has a row, not one row per app key. */
final class InboxStore extends SQLiteOpenHelper {
    private static final java.util.concurrent.atomic.AtomicLong revision=new java.util.concurrent.atomic.AtomicLong();
    static long revision(){return revision.get();}
    InboxStore(Context c){super(c,"notification_inbox.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,fingerprint TEXT UNIQUE NOT NULL,group_key TEXT NOT NULL,pkg TEXT NOT NULL,label TEXT NOT NULL,ts INTEGER NOT NULL,source TEXT NOT NULL,payload TEXT NOT NULL,state TEXT NOT NULL,reason TEXT NOT NULL,is_read INTEGER NOT NULL DEFAULT 0)");db.execSQL("CREATE INDEX inbox_group ON messages(group_key,ts)");db.execSQL("CREATE TABLE summaries(group_key TEXT PRIMARY KEY,payload TEXT NOT NULL,reviewed INTEGER NOT NULL,updated INTEGER NOT NULL)");}
    @Override public void onUpgrade(SQLiteDatabase db,int old,int next){}
    synchronized long capture(String fingerprint,String group,String pkg,String label,long ts,String source,String title,String text,String state,String reason) throws Exception {
        if(exists(fingerprint))return -1;
        ContentValues v=new ContentValues();v.put("fingerprint",fingerprint);v.put("group_key",group);v.put("pkg",pkg);v.put("label",label);v.put("ts",ts);v.put("source",source);v.put("payload",InboxCrypto.encrypt(new JSONObject().put("title",title).put("text",text).toString()));v.put("state",state);v.put("reason",reason);
        long id=getWritableDatabase().insertWithOnConflict("messages",null,v,SQLiteDatabase.CONFLICT_IGNORE);if(id>=0)revision.incrementAndGet();prune();return id;
    }
    private boolean exists(String fingerprint){try(Cursor c=getReadableDatabase().rawQuery("SELECT id FROM messages WHERE fingerprint=?",new String[]{fingerprint})){return c.moveToFirst();}}
    synchronized void prune(){SQLiteDatabase db=getWritableDatabase();int removed=db.delete("messages","ts<?",new String[]{String.valueOf(System.currentTimeMillis()-7L*86400000)});removed+=db.delete("messages","id NOT IN (SELECT id FROM messages ORDER BY ts DESC,id DESC LIMIT 500)",null);db.execSQL("DELETE FROM summaries WHERE group_key NOT IN (SELECT DISTINCT group_key FROM messages)");if(removed>0)revision.incrementAndGet();}
    private JSONObject row(Cursor c) throws JSONException {JSONObject o=new JSONObject();for(String key:new String[]{"group_key","pkg","label","source","state","reason"})o.put(key,c.getString(c.getColumnIndexOrThrow(key)));o.put("id",c.getLong(c.getColumnIndexOrThrow("id"))).put("ts",c.getLong(c.getColumnIndexOrThrow("ts"))).put("read",c.getInt(c.getColumnIndexOrThrow("is_read"))==1);try{JSONObject payload=new JSONObject(InboxCrypto.decrypt(c.getString(c.getColumnIndexOrThrow("payload"))));o.put("title",payload.optString("title")).put("text",payload.optString("text"));}catch(Exception e){o.put("title","Saved message").put("text","Content is unavailable on this device.");}return o;}
    synchronized JSONArray messages(boolean tests) throws JSONException{JSONArray out=new JSONArray();try(Cursor c=getReadableDatabase().query("messages",null,"source=?",new String[]{tests?"test":"real"},null,null,"ts DESC,id DESC","500")){while(c.moveToNext())out.put(row(c));}return out;}
    synchronized JSONArray pending(String group,int limit) throws JSONException{JSONArray out=new JSONArray();try(Cursor c=getReadableDatabase().query("messages",null,"group_key=? AND state='PENDING'",new String[]{group},null,null,"ts ASC,id ASC",String.valueOf(limit))){while(c.moveToNext())out.put(row(c));}return out;}
    synchronized JSONArray pendingGroups() {JSONArray out=new JSONArray();try(Cursor c=getReadableDatabase().rawQuery("SELECT DISTINCT group_key FROM messages WHERE state='PENDING' LIMIT 30",null)){while(c.moveToNext())out.put(c.getString(0));}return out;}
    synchronized void state(long id,String state,String reason){ContentValues v=new ContentValues();v.put("state",state);v.put("reason",reason);if(getWritableDatabase().update("messages",v,"id=?",new String[]{String.valueOf(id)})>0)revision.incrementAndGet();}
    synchronized void read(String group){ContentValues v=new ContentValues();v.put("is_read",1);getWritableDatabase().update("messages",v,group.isEmpty()?null:"group_key=?",group.isEmpty()?null:new String[]{group});revision.incrementAndGet();}
    synchronized void recoverReviewing(){getWritableDatabase().execSQL("UPDATE messages SET state='PENDING',reason='Review interrupted; waiting for retry.' WHERE state='REVIEWING'");revision.incrementAndGet();}
    synchronized void summary(String group,String text,int reviewed) throws Exception{ContentValues v=new ContentValues();v.put("group_key",group);v.put("payload",InboxCrypto.encrypt(text));v.put("reviewed",reviewed);v.put("updated",System.currentTimeMillis());getWritableDatabase().insertWithOnConflict("summaries",null,v,SQLiteDatabase.CONFLICT_REPLACE);revision.incrementAndGet();}
    synchronized JSONArray summaries(){JSONArray out=new JSONArray();try(Cursor c=getReadableDatabase().rawQuery("SELECT * FROM summaries",null)){while(c.moveToNext())try{out.put(new JSONObject().put("group_key",c.getString(0)).put("text",InboxCrypto.decrypt(c.getString(1))).put("reviewed",c.getInt(2)).put("updated",c.getLong(3)));}catch(Exception ignored){}}return out;}
    synchronized void clear(){getWritableDatabase().delete("messages",null,null);getWritableDatabase().delete("summaries",null,null);revision.incrementAndGet();}
    synchronized void retry(Context context){try(Cursor c=getReadableDatabase().rawQuery("SELECT id,pkg FROM messages WHERE state='UNKNOWN'",null)){while(c.moveToNext())if("SMART".equals(FocusUtils.roleFor(context,c.getString(1))))state(c.getLong(0),"PENDING","Queued for optional batch review.");}}
    synchronized void pauseReview(){ContentValues v=new ContentValues();v.put("state","UNKNOWN");v.put("reason","Gemini is off; saved for manual review.");int changed=getWritableDatabase().update("messages",v,"state IN ('PENDING','REVIEWING')",null);if(changed>0)revision.incrementAndGet();}
}

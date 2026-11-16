package com.attentiontwin.app;
import android.app.Notification;
import android.os.Bundle;
import java.util.List;
import androidx.core.app.NotificationCompat;

/** Extract useful messaging payloads rather than classifying only a generic summary. */
final class NotificationText {
 static final class Message {final String text;final long at;Message(String text,long at){this.text=text;this.at=at;}}
 static java.util.List<Message> messages(Notification notification){java.util.ArrayList<Message> out=new java.util.ArrayList<>();NotificationCompat.MessagingStyle style=NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);if(style!=null){List<NotificationCompat.MessagingStyle.Message> messages=style.getMessages();for(int i=Math.max(0,messages.size()-25);i<messages.size();i++){NotificationCompat.MessagingStyle.Message m=messages.get(i);if(m.getText()!=null)out.add(new Message((m.getSender()==null?"":clip(m.getSender().toString(),60)+": ")+clip(m.getText().toString(),500),m.getTimestamp()));}}if(out.isEmpty())out.add(new Message(clip(body(notification),500),notification.when));return out;}
 static String body(Notification notification){
  Bundle extras=notification.extras;if(extras==null)return "";
  NotificationCompat.MessagingStyle style=NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification);
  if(style!=null){List<NotificationCompat.MessagingStyle.Message> messages=style.getMessages();if(!messages.isEmpty()){NotificationCompat.MessagingStyle.Message m=messages.get(messages.size()-1);CharSequence sender=m.getSender(),text=m.getText();return (sender==null?"":clip(sender.toString(),40)+": ")+(text==null?"":clip(text.toString(),1500));}}
  CharSequence big=extras.getCharSequence(Notification.EXTRA_BIG_TEXT);if(big!=null&&!big.toString().trim().isEmpty())return clip(big.toString(),1500);
  CharSequence[] lines=extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);if(lines!=null&&lines.length>0&&lines[lines.length-1]!=null)return clip(lines[lines.length-1].toString(),1500);
  CharSequence text=extras.getCharSequence(Notification.EXTRA_TEXT);return text==null?"":text.toString();
 }
 private static String clip(String value,int n){return value.substring(0,Math.min(n,value.length()));}
}

package com.daybreak.clock;

import android.app.*;
import android.content.*;
import android.os.Build;
import org.json.*;
import java.util.*;

final class AlarmStore {
    static android.content.SharedPreferences prefs(Context c) { return c.getSharedPreferences("daybreak",0); }
    static JSONObject data(Context c) { try {return new JSONObject(prefs(c).getString("data","{\"alarms\":[],\"use24\":false,\"seconds\":true}"));}catch(JSONException e){return new JSONObject();} }
    static JSONArray alarms(Context c) { return data(c).optJSONArray("alarms")==null?new JSONArray():data(c).optJSONArray("alarms"); }
    static boolean canSchedule(Context c) { return Build.VERSION.SDK_INT<31||c.getSystemService(AlarmManager.class).canScheduleExactAlarms(); }
    static PendingIntent operation(Context c,long id) { return PendingIntent.getBroadcast(c,0,new Intent(c,AlarmReceiver.class).setData(android.net.Uri.parse("daybreak://alarm/"+id)).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE); }
    static PendingIntent show(Context c) {return PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static void save(Context c,String raw) throws JSONException {
        JSONObject next=new JSONObject(raw); JSONArray nextAlarms=next.getJSONArray("alarms");
        Set<Long> ids=new HashSet<>();
        for(int i=0;i<nextAlarms.length();i++){JSONObject a=nextAlarms.getJSONObject(i);long id=a.getLong("id");if(!ids.add(id))throw new JSONException("Duplicate alarm");if(!a.getString("time").matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]"))throw new JSONException("Invalid time");JSONArray days=a.getJSONArray("days");for(int j=0;j<days.length();j++)if(days.getInt(j)<0||days.getInt(j)>6)throw new JSONException("Invalid weekday");}
        AlarmManager manager=c.getSystemService(AlarmManager.class);JSONArray old=alarms(c);for(int i=0;i<old.length();i++)manager.cancel(operation(c,old.optJSONObject(i).optLong("id")));
        prefs(c).edit().putString("data",next.toString()).commit();reschedule(c);
    }
    static long next(JSONObject a,long now) {
        String[] time=a.optString("time","00:00").split(":");JSONArray days=a.optJSONArray("days");
        for(int offset=0;offset<8;offset++){Calendar d=Calendar.getInstance();d.setTimeInMillis(now);d.add(Calendar.DATE,offset);d.set(Calendar.HOUR_OF_DAY,Integer.parseInt(time[0]));d.set(Calendar.MINUTE,Integer.parseInt(time[1]));d.set(Calendar.SECOND,0);d.set(Calendar.MILLISECOND,0);int weekday=d.get(Calendar.DAY_OF_WEEK)-1;boolean allowed=days==null||days.length()==0;for(int j=0;days!=null&&j<days.length();j++)allowed|=days.optInt(j)==weekday;if(allowed&&d.getTimeInMillis()>now)return d.getTimeInMillis();}return -1;
    }
    static void reschedule(Context c) {
        if(!canSchedule(c))return;AlarmManager manager=c.getSystemService(AlarmManager.class);JSONArray all=alarms(c);
        for(int i=0;i<all.length();i++){JSONObject a=all.optJSONObject(i);if(a==null||!a.optBoolean("enabled"))continue;long when=next(a,System.currentTimeMillis());if(when>0)try{manager.setAlarmClock(new AlarmManager.AlarmClockInfo(when,show(c)),operation(c,a.optLong("id")));}catch(SecurityException ignored){}}
    }
    static JSONObject consume(Context c,long id) {
        JSONObject data=data(c);JSONArray all=alarms(c);JSONObject found=null;
        for(int i=0;i<all.length();i++){JSONObject a=all.optJSONObject(i);if(a!=null&&a.optLong("id")==id&&a.optBoolean("enabled")){found=a;try{if(a.optJSONArray("days")==null||a.optJSONArray("days").length()==0)a.put("enabled",false);}catch(JSONException ignored){}}}
        try{data.put("alarms",all);}catch(JSONException ignored){}prefs(c).edit().putString("data",data.toString()).commit();reschedule(c);return found;
    }
}

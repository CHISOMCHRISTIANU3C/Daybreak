package com.daybreak.clock;
import android.content.*;
import org.json.JSONObject;
public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){JSONObject a=AlarmStore.consume(c,i.getLongExtra("id",-1));if(a!=null)c.startForegroundService(new Intent(c,AlarmService.class).setAction("RING").putExtra("label",a.optString("label","Alarm")));}
}

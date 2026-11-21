package com.daybreak.clock;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.os.*;

public class AlarmService extends Service {
    private MediaPlayer player;private final Handler handler=new Handler(Looper.getMainLooper());private long rampStart;private float initial=.15f;
    private final Runnable ramp=new Runnable(){public void run(){if(player==null)return;float v=Math.min(1,initial+(1-initial)*(SystemClock.elapsedRealtime()-rampStart)/20000f);player.setVolume(v,v);if(v<1)handler.postDelayed(this,100);}};
    private final Runnable resume=()->{if(active(this)){AlarmStore.prefs(this).edit().putLong("scanUntil",0).commit();play(true);publish();}};
    static boolean active(Context c){return AlarmStore.prefs(c).getBoolean("ringing",false);}
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onCreate(){super.onCreate();NotificationChannel channel=new NotificationChannel("alarms","Ringing alarms",NotificationManager.IMPORTANCE_HIGH);channel.setSound(null,null);channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);getSystemService(NotificationManager.class).createNotificationChannel(channel);}
    private void foreground(){Notification n=new Notification.Builder(this,"alarms").setSmallIcon(com.daybreak.clock.R.drawable.ic_alarm).setContentTitle("Daybreak alarm").setContentText("Open to complete your wake-up scan").setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(AlarmStore.show(this)).setFullScreenIntent(AlarmStore.show(this),true).build();if(Build.VERSION.SDK_INT>=29)startForeground(41,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(41,n);}
    @Override public int onStartCommand(Intent i,int flags,int startId){String action=i==null?"RESTORE":i.getAction();
        if("DISMISS".equals(action)){AlarmStore.prefs(this).edit().putBoolean("ringing",false).putLong("scanUntil",0).commit();stopSelf();return START_NOT_STICKY;}
        if("RING".equals(action)){boolean already=active(this);AlarmStore.prefs(this).edit().putBoolean("ringing",true).putString("ringLabel",i.getStringExtra("label")).commit();if(!already)AlarmStore.prefs(this).edit().putLong("scanUntil",0).commit();}
        if(!active(this)){stopSelf();return START_NOT_STICKY;}foreground();handler.removeCallbacks(resume);
        long until=AlarmStore.prefs(this).getLong("scanUntil",0);
        if("SCAN".equals(action)&&until==0){until=System.currentTimeMillis()+30000;AlarmStore.prefs(this).edit().putLong("scanUntil",until).commit();}
        if("RESUME".equals(action)){until=0;AlarmStore.prefs(this).edit().putLong("scanUntil",0).commit();}
        if(until>System.currentTimeMillis()){silence();handler.postDelayed(resume,Math.max(1,until-System.currentTimeMillis()));}else{AlarmStore.prefs(this).edit().putLong("scanUntil",0).commit();play("RESUME".equals(action));}publish();return START_STICKY;
    }
    private void play(boolean loud){if(player!=null&&player.isPlaying())return;release();try{player=MediaPlayer.create(this,R.raw.classic_twin_bell,new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),0);if(player==null)return;player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);player.setLooping(true);initial=loud?1:.15f;player.setVolume(initial,initial);player.start();rampStart=SystemClock.elapsedRealtime();handler.post(ramp);}catch(RuntimeException e){release();}}
    private void silence(){handler.removeCallbacks(ramp);if(player!=null)player.pause();}
    private void release(){handler.removeCallbacks(ramp);if(player!=null){player.release();player=null;}}
    private void publish(){sendBroadcast(new Intent("com.daybreak.clock.STATE").setPackage(getPackageName()),"com.daybreak.clock.INTERNAL");}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);release();stopForeground(STOP_FOREGROUND_REMOVE);publish();super.onDestroy();}
}


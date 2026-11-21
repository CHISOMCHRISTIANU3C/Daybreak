package com.daybreak.clock;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.service.notification.Condition;

final class BedsidePolicy {
    static void set(Context c,boolean active){NotificationManager n=c.getSystemService(NotificationManager.class);if(!n.isNotificationPolicyAccessGranted())return;try{String id=AlarmStore.prefs(c).getString("bedsideRule",null);Uri condition=Uri.parse("condition://com.daybreak.clock/bedside");if(id==null||n.getAutomaticZenRule(id)==null){if(!active)return;AutomaticZenRule rule=new AutomaticZenRule("Daybreak bedside",null,new ComponentName(c,MainActivity.class),condition,null,NotificationManager.INTERRUPTION_FILTER_ALARMS,true);id=n.addAutomaticZenRule(rule);AlarmStore.prefs(c).edit().putString("bedsideRule",id).apply();}n.setAutomaticZenRuleState(id,new Condition(condition,"Bedside mode",active?Condition.STATE_TRUE:Condition.STATE_FALSE));}catch(SecurityException|IllegalArgumentException ignored){}}
}

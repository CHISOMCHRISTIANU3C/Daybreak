package com.daybreak.clock;
import android.content.Context;
import android.hardware.*;
import android.os.SystemClock;
import org.json.*;

final class IndoorTemperature implements SensorEventListener {
    private final SensorManager manager;
    private final Sensor sensor;
    private Float value;
    private long received=0;
    private boolean registered=false,reliable=true;
    IndoorTemperature(Context c){manager=c.getSystemService(SensorManager.class);sensor=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);}
    synchronized void start(){if(sensor!=null&&!registered){value=null;received=0;reliable=true;registered=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);}}
    synchronized void stop(){if(manager!=null)manager.unregisterListener(this);registered=false;}
    synchronized String state(){JSONObject o=new JSONObject();try{o.put("supported",sensor!=null);o.put("active",registered);o.put("reliable",reliable);o.put("onChange",sensor!=null&&sensor.getReportingMode()==Sensor.REPORTING_MODE_ON_CHANGE);o.put("sensorName",sensor==null?JSONObject.NULL:sensor.getName());o.put("celsius",value==null?JSONObject.NULL:value);o.put("ageMs",received==0?JSONObject.NULL:SystemClock.elapsedRealtime()-received);}catch(JSONException ignored){}return o.toString();}
    @Override public synchronized void onSensorChanged(SensorEvent e){if(e.sensor.getType()!=Sensor.TYPE_AMBIENT_TEMPERATURE||e.values.length==0)return;float v=e.values[0];if(Float.isFinite(v)&&v>=-50&&v<=85){value=v;received=SystemClock.elapsedRealtime();reliable=e.accuracy>SensorManager.SENSOR_STATUS_UNRELIABLE;}else{value=null;received=0;reliable=false;}}
    @Override public synchronized void onAccuracyChanged(Sensor s,int accuracy){reliable=accuracy>SensorManager.SENSOR_STATUS_UNRELIABLE;}
}


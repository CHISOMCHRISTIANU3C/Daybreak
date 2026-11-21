package com.daybreak.clock;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import org.json.*;
import java.util.*;
import java.util.function.Consumer;

final class WeatherLocation {
    private final Activity activity;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final List<CancellationSignal> signals=new ArrayList<>();
    private Consumer<JSONObject> callback;
    WeatherLocation(Activity a){activity=a;}
    void cancel(){for(CancellationSignal signal:signals)signal.cancel();signals.clear();handler.removeCallbacksAndMessages(null);callback=null;}
    void request(Consumer<JSONObject> result){cancel();callback=result;
        if(activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){fail("Location permission is needed for local weather.");return;}
        LocationManager manager=activity.getSystemService(LocationManager.class);
        if(manager==null||!manager.isLocationEnabled()){fail("Turn on device location to refresh the weather.");return;}
        List<String> providers=new ArrayList<>();for(String name:new String[]{"fused",LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER})if(manager.getAllProviders().contains(name)&&manager.isProviderEnabled(name))providers.add(name);
        if(providers.isEmpty()){fail("No location provider is available.");return;}
        handler.postDelayed(()->fail("Location timed out. Check device location and try again."),15000);
        final int[] remaining={providers.size()};
        for(String provider:providers){
            CancellationSignal signal=new CancellationSignal();signals.add(signal);
            try{manager.getCurrentLocation(provider,signal,activity.getMainExecutor(),location->{
                if(callback==null)return;
                if(location!=null){long age=(SystemClock.elapsedRealtimeNanos()-location.getElapsedRealtimeNanos())/1000000;
                    if(age>=0&&age<=300000){try{JSONObject o=new JSONObject();o.put("latitude",Math.round(location.getLatitude()*100)/100.0);o.put("longitude",Math.round(location.getLongitude()*100)/100.0);o.put("accuracy",location.getAccuracy());o.put("ageMs",age);complete(o);return;}catch(JSONException ignored){}}
                }
                if(--remaining[0]==0)fail("Current location is unavailable. Check device location and try again.");
            });}catch(SecurityException|IllegalArgumentException e){if(--remaining[0]==0)fail("Location access is unavailable. Check app permissions.");}
        }
    }
    private void fail(String message){JSONObject o=new JSONObject();try{o.put("error",message);}catch(JSONException ignored){}complete(o);}
    private void complete(JSONObject o){Consumer<JSONObject> done=callback;cancel();if(done!=null)done.accept(o);}
}

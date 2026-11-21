package com.daybreak.clock;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.speech.*;
import android.view.*;
import android.webkit.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity implements RecognitionListener {
    WebView web;VoiceFeedback voiceFeedback;AppUpdater updater;IndoorTemperature indoorTemperature;WeatherLocation weatherLocation;String pendingWeatherRequest; final Handler handler=new Handler(Looper.getMainLooper()); boolean loaded=false,visible=false,listening=false,wantsSpeech=false,bedside=false;SpeechRecognizer speech;PermissionRequest webPermission;
    final Runnable restart=()->listen();
    final BroadcastReceiver stateReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){sendState();}};
    @Override public void onCreate(Bundle state){super.onCreate(state);voiceFeedback=new VoiceFeedback(this,()->listen());updater=new AppUpdater(this);indoorTemperature=new IndoorTemperature(this);weatherLocation=new WeatherLocation(this);BedsidePolicy.set(this,false);getWindow().setStatusBarColor(android.graphics.Color.BLACK);getWindow().setNavigationBarColor(android.graphics.Color.BLACK);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);setShowWhenLocked(true);setTurnScreenOn(true);
        WebView.setWebContentsDebuggingEnabled((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE)!=0);web=new WebView(this);web.setBackgroundColor(android.graphics.Color.BLACK);setContentView(web);
        web.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);web.addJavascriptInterface(new Bridge(),"Android");
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest req){Uri u=req.getUrl();if(!"appassets.androidplatform.net".equals(u.getHost())||!"https".equals(u.getScheme()))return null;try{String path=u.getPath().substring(1);if(path.contains(".."))throw new IOException();if(path.isEmpty())path="index.html";String mime=path.endsWith(".html")?"text/html":path.endsWith(".js")||path.endsWith(".mjs")?"text/javascript":path.endsWith(".css")?"text/css":path.endsWith(".wav")?"audio/wav":path.endsWith(".wasm")?"application/wasm":"application/octet-stream";return new WebResourceResponse(mime,"UTF-8",200,"OK",Map.of("Cache-Control","no-store","Access-Control-Allow-Origin","https://appassets.androidplatform.net"),getAssets().open("ui/"+path));}catch(IOException e){return new WebResourceResponse("text/plain","UTF-8",404,"Not found",Map.of(),new ByteArrayInputStream(new byte[0]));}}
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !"appassets.androidplatform.net".equals(r.getUrl().getHost());}
            @Override public void onPageFinished(WebView v,String url){loaded=true;sendState();}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onPermissionRequest(PermissionRequest req){runOnUiThread(()->{if(!("https".equals(req.getOrigin().getScheme())&&"appassets.androidplatform.net".equals(req.getOrigin().getHost()))){req.deny();return;}if(webPermission!=null){req.deny();return;}webPermission=req;List<String> missing=new ArrayList<>();for(String r:req.getResources()){String p=r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)?Manifest.permission.CAMERA:r.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)?Manifest.permission.RECORD_AUDIO:null;if(p==null){req.deny();webPermission=null;return;}if(checkSelfPermission(p)!=PackageManager.PERMISSION_GRANTED)missing.add(p);}if(missing.isEmpty())grantWeb();else requestPermissions(missing.toArray(new String[0]),71);});}
            @Override public void onPermissionRequestCanceled(PermissionRequest r){if(webPermission==r)webPermission=null;}
            @Override public void onShowCustomView(View v,CustomViewCallback cb){cb.onCustomViewHidden();}
        });
        if(Build.VERSION.SDK_INT>=33)registerReceiver(stateReceiver,new IntentFilter("com.daybreak.clock.STATE"),Context.RECEIVER_NOT_EXPORTED);else registerLegacyReceiver();
        web.loadUrl("https://appassets.androidplatform.net/index.html?device="+(getResources().getConfiguration().smallestScreenWidthDp>=600?"tablet":"phone"));
    }
    // Android 11/12 lack RECEIVER_NOT_EXPORTED; protect this receiver with our signature permission instead.
    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
    void registerLegacyReceiver(){registerReceiver(stateReceiver,new IntentFilter("com.daybreak.clock.STATE"),"com.daybreak.clock.INTERNAL",handler);}
    void grantWeb(){if(webPermission==null)return;List<String> grants=new ArrayList<>();for(String r:webPermission.getResources()){if(r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)&&checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)grants.add(r);if(r.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)grants.add(r);}if(grants.isEmpty())webPermission.deny();else webPermission.grant(grants.toArray(new String[0]));webPermission=null;}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==71)grantWeb();if(request==73&&pendingWeatherRequest!=null){String id=pendingWeatherRequest;pendingWeatherRequest=null;handler.postDelayed(()->requestWeatherLocation(id,false),100);}sendState();}
    @Override protected void onResume(){super.onResume();visible=true;updater.onResume();indoorTemperature.start();if(AlarmService.active(this))startForegroundService(new Intent(this,AlarmService.class).setAction("RESTORE"));BedsidePolicy.set(this,bedside);AlarmStore.reschedule(this);sendState();if(wantsSpeech)listen();}
    @Override protected void onPause(){visible=false;indoorTemperature.stop();weatherLocation.cancel();voiceFeedback.cancel();BedsidePolicy.set(this,false);stopSpeech();super.onPause();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);sendState();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);updater.close();voiceFeedback.destroy();unregisterReceiver(stateReceiver);if(speech!=null)speech.destroy();web.removeJavascriptInterface("Android");web.destroy();super.onDestroy();}
    void js(String script){if(loaded)web.evaluateJavascript(script,null);}
    void sendState(){handler.post(()->{try{JSONObject d=new JSONObject();d.put("ringing",AlarmService.active(this));d.put("label",AlarmStore.prefs(this).getString("ringLabel","Alarm"));d.put("scanUntil",AlarmStore.prefs(this).getLong("scanUntil",0));d.put("exact",AlarmStore.canSchedule(this));d.put("dnd",getSystemService(NotificationManager.class).isNotificationPolicyAccessGranted());d.put("notifications",getSystemService(NotificationManager.class).areNotificationsEnabled());d.put("fullScreen",Build.VERSION.SDK_INT<34||getSystemService(NotificationManager.class).canUseFullScreenIntent());d.put("alarms",AlarmStore.data(this));js("window.onNativeState&&window.onNativeState("+d+")");}catch(JSONException ignored){}});}
    class Bridge {
        @JavascriptInterface public void weatherLocation(String requestId,boolean prompt){handler.post(()->requestWeatherLocation(requestId,prompt));}
        @JavascriptInterface public void cancelWeatherLocation(){handler.post(()->weatherLocation.cancel());}
        @JavascriptInterface public String updateInfo(){return updater.state();}
        @JavascriptInterface public void checkUpdates(){handler.post(()->updater.check());}
        @JavascriptInterface public void downloadUpdate(){handler.post(()->updater.download());}
        @JavascriptInterface public void installUpdate(){handler.post(()->updater.install());}
        @JavascriptInterface public String ambientTemperature(){return indoorTemperature.state();}
        @JavascriptInterface public void weatherAttribution(){handler.post(()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://open-meteo.com/"))));}
        @JavascriptInterface public boolean canScheduleAlarms(){return AlarmStore.canSchedule(MainActivity.this);}
        @JavascriptInterface public void voiceFeedback(String kind,String text){handler.post(()->{MainActivity.this.stopSpeech();MainActivity.this.voiceFeedback.play(kind,text);});}
        @JavascriptInterface public String load(){return AlarmStore.data(MainActivity.this).toString();}
        @JavascriptInterface public boolean save(String data){try{AlarmStore.save(MainActivity.this,data);handler.post(()->{if(!AlarmStore.canSchedule(MainActivity.this))new AlertDialog.Builder(MainActivity.this).setTitle("Allow precise alarms").setMessage("Your alarm is saved but cannot ring until Alarms & reminders access is allowed.").setPositiveButton("Open settings",(d,w)->openSpecial("exact")).setNegativeButton("Later",null).show();});return true;}catch(Exception e){return false;}}
        @JavascriptInterface public void settings(String kind){handler.post(()->openSpecial(kind));}
        @JavascriptInterface public void testAlarm(){handler.post(()->startForegroundService(new Intent(MainActivity.this,AlarmService.class).setAction("RING").putExtra("label","Test alarm")));}
        @JavascriptInterface public void scan(){handler.post(()->{if(AlarmService.active(MainActivity.this))startService(new Intent(MainActivity.this,AlarmService.class).setAction("SCAN"));});}
        @JavascriptInterface public void scanResult(boolean pass){handler.post(()->{if(AlarmService.active(MainActivity.this)){long until=AlarmStore.prefs(MainActivity.this).getLong("scanUntil",0);boolean valid=pass&&until>System.currentTimeMillis();startService(new Intent(MainActivity.this,AlarmService.class).setAction(valid?"DISMISS":"RESUME"));}});}
        @JavascriptInterface public void startSpeech(){handler.post(()->{wantsSpeech=true;listen();});}
        @JavascriptInterface public void stopSpeech(){handler.post(()->{wantsSpeech=false;MainActivity.this.stopSpeech();});}
        @JavascriptInterface public void bedside(boolean on){handler.post(()->{bedside=on;BedsidePolicy.set(MainActivity.this,on);if(on)getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());else getWindow().getInsetsController().show(WindowInsets.Type.systemBars());});}
    }
    void requestWeatherLocation(String requestId,boolean prompt){
        if(!visible)return;
        if(prompt&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){pendingWeatherRequest=requestId;requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},73);return;}
        weatherLocation.request(data->js("window.onWeatherLocation&&onWeatherLocation("+JSONObject.quote(requestId)+","+data+")"));
    }
    void openSpecial(String kind){try{Intent intent;if(kind.equals("pin")){startLockTask();return;}if(kind.equals("dnd"))intent=new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);else if(kind.equals("exact")&&Build.VERSION.SDK_INT>=31)intent=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()));else if(kind.equals("fullScreen")&&Build.VERSION.SDK_INT>=34)intent=new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName()));else if(kind.equals("notifications")&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},72);return;}else intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()));startActivity(intent);}catch(ActivityNotFoundException ignored){}}
    void listen(){if(!visible||!wantsSpeech||listening||voiceFeedback.busy)return;if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){js("window.nativeSpeechError&&nativeSpeechError(\"not-allowed\")");return;}if(!SpeechRecognizer.isRecognitionAvailable(this)){js("window.nativeSpeechError&&nativeSpeechError('service-not-allowed')");return;}if(speech==null){speech=SpeechRecognizer.createSpeechRecognizer(this);speech.setRecognitionListener(this);}Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);listening=true;try{speech.startListening(i);}catch(RuntimeException e){listening=false;}}
    void stopSpeech(){handler.removeCallbacks(restart);listening=false;if(speech!=null)speech.cancel();}
    @Override public void onResults(Bundle b){listening=false;ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(r!=null&&!r.isEmpty())js("window.nativeSpeechResult&&nativeSpeechResult("+JSONObject.quote(r.get(0))+")");if(visible&&wantsSpeech)handler.postDelayed(restart,700);}
    @Override public void onError(int e){listening=false;if(e==SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){js("window.nativeSpeechError&&nativeSpeechError('not-allowed')");return;}if(visible&&wantsSpeech)handler.postDelayed(restart,e==SpeechRecognizer.ERROR_RECOGNIZER_BUSY?2500:1200);}
    @Override public void onReadyForSpeech(Bundle b){} @Override public void onBeginningOfSpeech(){} @Override public void onRmsChanged(float r){} @Override public void onBufferReceived(byte[] b){} @Override public void onEndOfSpeech(){} @Override public void onPartialResults(Bundle b){} @Override public void onEvent(int e,Bundle b){}
}






package com.daybreak.clock;

import android.content.Context;
import android.media.*;
import android.os.*;
import android.speech.tts.*;
import java.util.Locale;

final class VoiceFeedback {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable resume;
    private final ToneGenerator tones;
    private TextToSpeech tts;
    private boolean ready=false,failed=false;
    boolean busy=false;
    private long generation=0;
    private String pending;
    VoiceFeedback(Context context,Runnable resume){
        this.resume=resume;ToneGenerator tone=null;try{tone=new ToneGenerator(AudioManager.STREAM_ALARM,65);}catch(RuntimeException ignored){}tones=tone;
        tts=new TextToSpeech(context,status->{ready=status==TextToSpeech.SUCCESS;failed=!ready;if(ready){tts.setLanguage(Locale.US);tts.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());}if(pending!=null){String text=pending;pending=null;speak(generation,text);}});
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){public void onStart(String id){}public void onDone(String id){handler.post(()->finishId(id));}public void onError(String id){handler.post(()->finishId(id));}});
    }
    void play(String kind,String text){long token=++generation;busy=true;pending=null;if(kind.equals("heard")){if(tones!=null)tones.startTone(ToneGenerator.TONE_PROP_BEEP,120);handler.postDelayed(()->finish(token),180);}else{handler.postDelayed(()->speak(token,text),160);}handler.postDelayed(()->finish(token),12000);}
    private void speak(long token,String text){if(token!=generation)return;if(!ready&&!failed){pending=text;return;}if(!ready){if(tones!=null)tones.startTone(ToneGenerator.TONE_PROP_ACK,220);handler.postDelayed(()->finish(token),300);return;}int result=tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,Long.toString(token));if(result==TextToSpeech.ERROR)finish(token);}
    private void finishId(String id){try{finish(Long.parseLong(id));}catch(NumberFormatException ignored){}}
    private void finish(long token){if(token!=generation)return;pending=null;busy=false;handler.postDelayed(resume,400);}
    void cancel(){++generation;pending=null;busy=false;handler.removeCallbacksAndMessages(null);if(tts!=null)tts.stop();if(tones!=null)tones.stopTone();}
    void destroy(){cancel();if(tts!=null)tts.shutdown();if(tones!=null)tones.release();}
}

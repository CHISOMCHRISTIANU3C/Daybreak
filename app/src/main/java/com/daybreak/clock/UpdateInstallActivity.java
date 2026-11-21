package com.daybreak.clock;
import android.app.*;
import android.content.*;
import android.content.pm.PackageInstaller;
import android.os.Bundle;

public class UpdateInstallActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);handle(getIntent());}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);handle(intent);}
    private void handle(Intent intent){int status=intent.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);if(status==PackageInstaller.STATUS_PENDING_USER_ACTION){Intent confirmation=intent.getParcelableExtra(Intent.EXTRA_INTENT);if(confirmation!=null){try{startActivity(confirmation);finish();return;}catch(RuntimeException ignored){}}show("Android could not open the update confirmation. Please try again.");}else if(status==PackageInstaller.STATUS_SUCCESS){AlarmStore.prefs(this).edit().putString("updateResult","Daybreak updated successfully.").apply();AlarmStore.reschedule(this);startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();}else show(status==PackageInstaller.STATUS_FAILURE_ABORTED?"Update cancelled. Your installed app and saved alarms are unchanged.":"Android could not install the update. Your installed app and saved alarms are unchanged.");}
    private void show(String message){AlarmStore.prefs(this).edit().putString("updateResult",message).apply();new AlertDialog.Builder(this).setTitle("Daybreak update").setMessage(message).setPositiveButton("Return to Daybreak",(d,w)->{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();}).setOnCancelListener(d->finish()).show();}
}

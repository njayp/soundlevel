package com.doepiccoding.voiceanalizer;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;


public class UpdateService extends Service {
    static int media_max_volume = 1;        // to avoid division by zero

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (prefs.getBoolean("enabled",true) == true && prefs.getLong("acceleration",0) < 1.5) {
            AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int sound_level = prefs.getInt("media_volume", 0);
            Log.d("Update service","sound level "+sound_level);
            media_max_volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            Log.d("Update service", "Max volume on this thing: "+media_max_volume);
            int normalised_sound_level = media_max_volume - ((sound_level - 0) / (media_max_volume - 0));      // normalising sound level between 0 and max volume range supported by phone
            // normalised_sound -> direct function of sound levels detected
            Log.d("Update service","Normalised sound levels: "+normalised_sound_level);
            //Set Media Volume
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, normalised_sound_level, AudioManager.FLAG_SHOW_UI);
            RemoteViews view = new RemoteViews(getPackageName(), R.layout.updating_widget);
            Log.d("Update service", "" + normalised_sound_level);
            view.setTextViewText(R.id.tvWidget, "Volume: " + normalised_sound_level);
            ComponentName theWidget = new ComponentName(this, UpdatingWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(theWidget, view);
        }else{
            Log.d("Update Service","Don't update media volume; user has disabled/ user is in motion");
        }
        return super.onStartCommand(intent, flags, startId);
    }

}

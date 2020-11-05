package com.doepiccoding.voiceanalizer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class VoiceSensorService extends Service {

    protected Handler handler;
    private static final int sampleRate = 8000;
    private AudioRecord audio;
    private int bufferSize;
    private static final int SAMPLE_DELAY = 1000;
    private double soundLevel = 0;
    private Thread thread;
    private static SharedPreferences preferences ;
    private static SharedPreferences.Editor editor ;

    public VoiceSensorService(Context applicationContext) {
        super();
        Log.i("Voice Sensor service", "here I am!");
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        editor = preferences.edit();
    }

    public VoiceSensorService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start recording
        Log.d("Voice Sensor service","started");
        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }
        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        audio.startRecording();
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
            thread = new Thread(new Runnable() {
                public void run() {
                    while (thread != null && !thread.isInterrupted()) {
                        //Let's make the thread sleep for a the approximate sampling time
                        try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                        readAudioBuffer();//After this call we can get the last value assigned to the lastLevel variable
                        Log.d("Voice Sensor service",""+(int)soundLevel);
                        editor.putInt("media_volume",(int)soundLevel);
                        editor.commit();
                    }
                }
            });
            thread.start();
            }
        });

        //stopSelf();
        return START_STICKY;
    }

    /**
     * Functionality that gets the sound level out of the sample
     */
    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult = 1;

            if (audio != null) {

                // Sense the voice...
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                double sumLevel = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
                }
                soundLevel = Math.abs((sumLevel / bufferReadResult));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("EXIT", "onDestroy!");
    }

}

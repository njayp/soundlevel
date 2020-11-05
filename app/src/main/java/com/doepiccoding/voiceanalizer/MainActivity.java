package com.doepiccoding.voiceanalizer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private ImageView mouthImage;
    private ToggleButton toggleButton;
    Intent mSoundIntent;
    Intent mMovementIntent;
    private static SharedPreferences preferences ;
    private static SharedPreferences.Editor editor ;
    private VoiceSensorService mSensorService;
    private MovementService mtrackerService;
    Context ctx;
    public Context getCtx() {
        return ctx;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_main);

		mouthImage = (ImageView)findViewById(R.id.mounthHolder);
		mouthImage.setKeepScreenOn(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        editor = preferences.edit();
        toggleButton = (ToggleButton)findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    editor.putBoolean("enabled",true);
                } else {
                    // The toggle is disabled
                    editor.putBoolean("enabled",false);
                }
                editor.commit();
            }
        });

		// start recording
        mSensorService = new VoiceSensorService(getCtx());
        mSoundIntent = new Intent(getCtx(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mSoundIntent);
        }

        // start tracking movement
        mtrackerService = new MovementService(getCtx());
        mMovementIntent = new Intent(getCtx(), mtrackerService.getClass());
        if (!isMyServiceRunning(mtrackerService.getClass())) {
            startService(mMovementIntent);
        }


	}

	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mSoundIntent);
        stopService(mMovementIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();
    }
}

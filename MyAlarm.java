package com.example.watch2mobius;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

public class MyAlarm extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {
        MediaPlayer mediaplayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        mediaplayer.setVolume(0,0);
        mediaplayer.start();

    }
}

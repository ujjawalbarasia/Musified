package com.example.android.musified;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

/**
 * Created by Shubhi on 13-07-2016.
 */
public class SplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_splash_screen);
        Thread background = new Thread() {
            public void run() {

                try {
                    sleep(1500);        // After 1 seconds play Music
                    final MediaPlayer mp = MediaPlayer.create(SplashScreen.this,R.raw.audio);
                    mp.start();         //Start Playing
                    sleep(2000);        // After 2 seconds redirect to another intent
                    Intent i=new Intent(getBaseContext(),MainActivity.class);
                    startActivity(i);   //Jump to Mainactivity
                    finish();           //Remove activity

                } catch (Exception e) {

                }
            }
        };

        // start thread
        background.start();
    }
}
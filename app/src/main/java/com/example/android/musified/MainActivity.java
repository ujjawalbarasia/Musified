package com.example.android.musified;

import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.*;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import com.example.android.musified.R;
import  com.example.android.musified.MusicService;
import com.example.android.musified.Song;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,MediaPlayerControl {
    ArrayList<Song> songList;                               //Song ArrayList
    ListView songView;                                      //ListView for Songs
    private MusicController controller;                     //MediaController Inbuilt
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private boolean paused = false, playbackPaused = false;
    SensorManager mySensorManager;                          //for Proximity Sensor
    Sensor myProximitySensor;                               //Proximity Sensor
    float [] sensvalues;                                    //Accelerometer Values
    SensorManager sensmanager;                              //for Accelerometer Sensor
    Sensor sens;                                            //Accelerometer Sensor
    int pEnable=0, aEnable=0;                               //Variables to On/Off the sensors
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "";    //Folder to save Recordings
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,             MediaRecorder.OutputFormat.THREE_GPP };
    ImageButton repeatBtn, vdBtn,muteBtn, vuBtn, shuffleBtn;
    //private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };
    boolean rEnable=false;
    private Camera cam;
    private boolean on;
    Camera.Parameters params;
    int delay = 100;
    Boolean sEnable=false;
    Boolean bEnable=false;
    BluetoothAdapter mBluetoothAdapter;
    MenuItem BluetoothItem;
    private SeekBar volumeSeekbar= null;
    private AudioManager audioManager= null;
    TextView volText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        volText=(TextView)findViewById(R.id.volText);
        volumeSeekbar= (SeekBar)findViewById(R.id.seekBar);
        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeSeekbar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekbar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        songView = (ListView) findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Context context = this;
        PackageManager pm = context.getPackageManager();

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            return;
        }


        //controller.show(0);
        //Sensor-proximity
        repeatBtn=(ImageButton)findViewById(R.id.repeat_button);
        repeatBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(rEnable==false) {
                    rEnable = true;
                    repeatBtn.setBackgroundResource(R.drawable.repeat);
                }
                else
                {
                    rEnable=false;
                    repeatBtn.setBackgroundResource(R.drawable.repeat_not);
                }
                    musicSrv.setRepeat();
            }
        });
        shuffleBtn=(ImageButton)findViewById(R.id.shuffle_button);
        shuffleBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(sEnable==false) {
                    sEnable = true;
                    shuffleBtn.setBackgroundResource(R.drawable.shuffle);
                }
                else
                {
                    sEnable=false;
                    shuffleBtn.setBackgroundResource(R.drawable.shuffle_not);
                }
                musicSrv.setShuffle();
            }
        });

        mySensorManager = (SensorManager)getSystemService(
                Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);
        if (myProximitySensor == null){
            Toast.makeText(getApplicationContext(),"No Proximity Sensor! Flip will not work",Toast.LENGTH_LONG).show();
        }else{
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        //sensor-accelerometer
        sensmanager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sens=sensmanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sens == null){
            Toast.makeText(MainActivity.this, "No accelerometer Sensor! Shake will not work", Toast.LENGTH_LONG).show();
        }else{
            sensmanager.registerListener(accelerometerSensorEventListener,
                    sens,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }





        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                fab.setImageResource(android.R.drawable.presence_audio_away);
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //AppLog.logString("Start Recording");
                        Toast.makeText(getApplicationContext(),"Recording Started",Toast.LENGTH_LONG).show();
                        startRecording();
                        break;
                    case MotionEvent.ACTION_UP:
                        //AppLog.logString("stop Recording");
                        Toast.makeText(getApplicationContext(),"Recording Stopped",Toast.LENGTH_LONG).show();
                        stopRecording();
                        Toast.makeText(getApplicationContext(),"Recording Stopped",Toast.LENGTH_LONG).show();
                        fab.setImageResource(android.R.drawable.presence_audio_online);
                        break;

                }
                return false;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setController();
    }

    SensorEventListener accelerometerSensorEventListener
            = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(aEnable==1)
            {
            sensvalues=event.values;
            float x=sensvalues[0];
            float y=sensvalues[1];
            float z=sensvalues[2];
            if(x>17||y>17||z>17)
            {
                Toast.makeText(getApplicationContext(),"Next Track",Toast.LENGTH_LONG).show();
                musicSrv.playNext();
                //seekTo(0);
                if(playbackPaused){
                    //setController();
                    playbackPaused = false;
                }
                //controller.show(0);
            }}
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    SensorEventListener proximitySensorEventListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
                if(pEnable==1)
                {
                if (event.values[0] == 0) {
                    //Toast.makeText(MainActivity.this,"Near",Toast.LENGTH_SHORT).show();
                    if(isPlaying())
                    {}
                    else {
                        playbackPaused=false;
                        musicSrv.go();
                        setController();

                        //controller.setAnchorView(findViewById(R.id.main_audio_view));
                        //controller.setMediaPlayer(this);
                        //setController();


                        //controller.show(0);
                    }
                } else {
                    if(isPlaying())
                    {
                        playbackPaused=true;
                        musicSrv.pausePlayer();
                        setController();
                    }
                    //Toast.makeText(MainActivity.this,"Far",Toast.LENGTH_SHORT).show();

                }
            }}
        }

    };

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }



    //user song select
    public void songPicked(View view){
        //String a = v.getTag().toString();
        //Toast.makeText(getApplicationContext(),a,Toast.LENGTH_SHORT).show();
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        if(!isPlaying())
        {
            controller.show(0);
        }
        //setController();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            drawer.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        BluetoothItem = menu.findItem(R.id.action_bluetooth);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        switch(id){
            case R.id.action_search:onSearchRequested();
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) findViewById(R.id.action_search);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                 break;
            case R.id.action_bluetooth:
                if(bEnable==false) {
                    bEnable=true;
                    mBluetoothAdapter.enable();
                    BluetoothItem.setIcon(R.drawable.bt_on_icon_new);
                    Toast.makeText(getApplicationContext(),"Bluetooth ON",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),"Searching for Nearby Devices",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    bEnable=false;
                    mBluetoothAdapter.disable();
                    BluetoothItem.setIcon(R.drawable.bt_icon);
                    Toast.makeText(getApplicationContext(),"Bluetooth OFF",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_fav) {

        } else if (id == R.id.nav_acc) {
            if (aEnable == 0)
            {
                aEnable=1;
                Toast.makeText(MainActivity.this,"Sense-Shake Enabled",Toast.LENGTH_SHORT).show();
            }
            else
            {
                aEnable=0;
                Toast.makeText(MainActivity.this,"Sense-Shake Disabled",Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_pro) {
            if (pEnable == 0)
            {
                pEnable=1;
                Toast.makeText(MainActivity.this,"Sense-Flip Enabled",Toast.LENGTH_SHORT).show();
            }
            else
            {
                pEnable=0;
                Toast.makeText(MainActivity.this,"Sense-Flip Disabled",Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_flash) {
            if(rEnable==false)
            {
                rEnable=true;
                flash_effect();
            }
            else if(rEnable==true)
            {
                rEnable=false;
            }
        }else if (id == R.id.nav_youtube) {
            Intent i=new Intent(this,yview.class);
            startActivity(i);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getSongList(){
        //query external audio
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng()){
            return musicSrv.getDur();
        }
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController() {
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }



    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3");
    }
    private void startRecording(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private MediaRecorder.OnErrorListener errorListener = new        MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            //AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            //AppLog.logString("Warning: " + what + ", " + extra);
        }
    };
    private void stopRecording(){
        if(null != recorder){
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    public void flash_effect() {
        cam = Camera.open();
        final Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);


        Thread t = new Thread() {
            public void run() {
                try {
                    // Switch on the cam for app's life
                    if (cam == null) {
                        // Turn on Cam
                        cam = Camera.open();
                        try {
                            cam.setPreviewDisplay(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cam.startPreview();
                    }

                    while(rEnable==true) {
                        if (isPlaying()) {
                            for (int i = 0; i < 2; i++) {
                                toggleFlashLight();
                                sleep(delay);
                            }
                        }
                    }

                    if (cam != null) {
                        cam.stopPreview();
                        cam.release();
                        cam = null;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        t.start();
    }
    public void turnOn() {
        if (cam != null) {
            // Turn on LED
            params = cam.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(params);

            on = true;
        }
    }

    /** Turn the devices FlashLight off */
    public void turnOff() {
        // Turn off flashlight
        if (cam != null) {
            params = cam.getParameters();
            if (params.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                cam.setParameters(params);
            }
        }
        on = false;
    }

    /** Toggle the flashlight on/off status */
    public void toggleFlashLight() {
        if (!on) { // Off, turn it on
            turnOn();
        } else { // On, turn it off
            turnOff();
        }
    }
    private void initControls()
    {
        try
        {
            volumeSeekbar= (SeekBar)findViewById(R.id.seekBar);
            audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                    volText.setText("Volume : "+progress);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}

package com.example.david.gameapp.utils;

import android.app.Activity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.media.SoundPool;


import com.example.david.gameapp.R;


public class SoundHelper {

    private MediaPlayer mMusicPlayer;

    private SoundPool mSoundPool;
    private int mSoundID1; // balloon pop sound
    private int mSoundID2; //click sound
    private boolean mLoaded;
    private float mVolume;

    public SoundHelper(Activity activity) {

        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        mVolume = .2f;

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(6).build();
        } else {
            //noinspection deprecation
            mSoundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });
        mSoundID1 = mSoundPool.load(activity, R.raw.balloon_pop, 1);
        mSoundID2 = mSoundPool.load(activity, R.raw.click, 1);
    }

    public void playPop() {   //for sound effects
        if (mLoaded) {
            mSoundPool.play(mSoundID1, mVolume, mVolume, 1, 0, .9f);
        }
    }

   public void playClick() {   //for sound effects

        if (mLoaded) {
            mSoundPool.play(mSoundID2, mVolume + .02f, mVolume + .02f, 1, 0, 1f);
        }
    }



   public void  prepareMusicPlayer(Context context){    //setting the music in the background and looping it.
     mMusicPlayer = MediaPlayer.create(context.getApplicationContext(),
         R.raw.pleasant_music);
         mMusicPlayer.setVolume(.4f, .4f);
         mMusicPlayer.setLooping(true);

    }

    public void playMusic(){  //play BG music.
          if (mMusicPlayer != null){
              mMusicPlayer.start();
          }

    }

    public void pauseMusic(){   //Pauses BG music
        if (mMusicPlayer != null && mMusicPlayer.isPlaying()){
            mMusicPlayer.pause();
        }
    }



}

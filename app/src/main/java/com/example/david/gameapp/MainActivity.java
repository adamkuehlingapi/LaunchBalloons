package com.example.david.gameapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.gameapp.utils.SimpleAlertDialog;
import com.example.david.gameapp.utils.SoundHelper;
import com.example.david.gameapp.utils.hiScoreHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity

    implements Balloon.BalloonListener{

    private ViewGroup mContentView;

    private int[] mBalloonColors = new int[3];
    private int mNextColor, mScreenWidth, mScreenHeight;
    public static final int balloonSpeedFast = 1000; //time in miliseconds to get to top of screen
    public static final int balloonSpeedSlow = 7000;
    public static final int balloonFreqMax = 2000;
    public static final int balloonFreqMin =  800;
    private int level = 0;
    private int score = 0;
    private int hiScore = 0;
    int balloonsLaunched = 0;
    private int pinsUsed = 0;
    private SoundHelper mSoundHelper;

    private boolean isGameOver = false;

    private boolean levelDone = true;

    private List<Balloon> balloonList= new ArrayList<>();

    private List<ImageView> pinImages = new ArrayList<>();

    TextView mLevel, mScore, button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBalloonColors[0] = Color.argb(200, 15, 200, 15);
        mBalloonColors[1] = Color.argb(200, 0, 255, 0);
        mBalloonColors[2] = Color.argb(200, 50, 150, 50);

        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);

        mSoundHelper = new SoundHelper(this);

        mSoundHelper.prepareMusicPlayer(this);



        mContentView = (ViewGroup) findViewById(R.id.activity_main);
        setToFullScreen();

        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight() ;
                }
            });
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToFullScreen();
            }
        });




        mContentView.setOnTouchListener(new View.OnTouchListener() {
        @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                 //   Balloon b = new Balloon(MainActivity.this, mBalloonColors[mNextColor], 100);
                 //   b.setX(motionEvent.getX()); // see's where your mouse click was horizontally.
                  //  b.setY(mScreenHeight);
                  //  mContentView.addView(b);
                   // b.releaseBalloon(mScreenHeight, 2000);  //how fast the balloon moves

                    if (mNextColor + 1 == mBalloonColors.length) {
                        mNextColor = 0;
                    } else {
                        mNextColor++;
                    }

                }

                return false;
            }
        });



        mLevel = (TextView) findViewById(R.id.level_display);
        button = (TextView) findViewById(R.id.go_button);
        mScore = (TextView) findViewById(R.id.score_display);
        pinImages.add((ImageView) findViewById(R.id.pushpin1));
        pinImages.add((ImageView) findViewById(R.id.pushpin2));
        pinImages.add((ImageView) findViewById(R.id.pushpin3));
        pinImages.add((ImageView) findViewById(R.id.pushpin4));
        pinImages.add((ImageView) findViewById(R.id.pushpin5));


    }



    private void setToFullScreen() {
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.activity_main);
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }



    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {
        mContentView.removeView(balloon);
        mSoundHelper.playPop();
        balloonList.remove(balloon);
        if (userTouch) {
            score++;
            updateDisplay();
        } else {
            pinsUsed ++;
            if (pinsUsed <= pinImages.size()){
                pinImages.get(pinsUsed-1).setImageResource(R.drawable.pin_off);   //sets the drawable for this pin to pin off
            }

            if (pinsUsed == pinImages.size()){
                gameOver();
               }
            }




    }



     private void gameOver() {
        mSoundHelper.pauseMusic();


        int tScore = hiScoreHelper.getTopScore(this);
        if (tScore < 1) {    //if its not set, set whatever the current score is.
            hiScoreHelper.setTopScore(this, score);

            SimpleAlertDialog dialog = SimpleAlertDialog.newInstance("New High Score!",
                    String.format("Your High Score: %d", score));
            dialog.show(getSupportFragmentManager(), null);
            dialog.setCancelable(false);
        } else if (hiScoreHelper.isTopScore(this,score)){
             hiScoreHelper.setTopScore(this, score);

            SimpleAlertDialog dialog = SimpleAlertDialog.newInstance("New High Score!",
                    String.format("Your High Score: %d", score));
            dialog.show(getSupportFragmentManager(), null);
            dialog.setCancelable(false);
         } else {
            Toast.makeText(this,"Game Over. Try again; press NEW GAME.",Toast.LENGTH_SHORT).show();
        }



         for (Balloon loon : balloonList) {
            mContentView.removeView(loon);
             loon.setPopped(true);

         }
         isGameOver = true;

         balloonList.clear();
         updateDisplay();
     }



    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];
            int maxDelay = Math.max(balloonFreqMin,
                    (balloonFreqMax- ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

            balloonsLaunched = 0;
            while (balloonsLaunched < 3 * level) {

                if (!isGameOver) {


//              Get a random horizontal position for the next balloon
                    Random random = new Random(new Date().getTime());
                    int xPosition = random.nextInt(mScreenWidth - 200);
                    publishProgress(xPosition);
                    balloonsLaunched++;

//              Wait a random number of milliseconds before looping
                    int delay = random.nextInt(minDelay) + minDelay;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return null;
                }

            }
            return null;


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }

    }

    private void launchBalloon(int x) {

        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150);
        balloonList.add(balloon);

        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

        Random random = new Random();
        int randomSpeed = (100 * level) + random.nextInt( (balloonSpeedFast * level) - 200 );

//      Let 'er fly
        int duration = Math.max(balloonSpeedFast, 300 + (balloonSpeedSlow - randomSpeed));
        balloon.releaseBalloon(mScreenHeight, duration);

        if (balloonsLaunched == level * 3)
        {
            levelEnd();
        }

    }

    public void levelStart(){
        mSoundHelper.playMusic();

        if (isGameOver){

            score=0;
            level=0;
            pinsUsed = 0;

            isGameOver = false;
            levelDone=true;
            int i = 0;
            while (i < 5){
                pinImages.get(i).setImageResource(R.drawable.pin);   //sets the drawable for this pin off to pin.
            i++;
            }



        }


        if (levelDone && !(isGameOver)) {  // If its stopped and not game over, that means proceed to next level.
         level++;
         BalloonLauncher launcher = new BalloonLauncher();
         launcher.execute(level);


            Handler handler = new Handler();

            handler.postDelayed(new Runnable() { //delays action by one second before starting the level.
                @Override
                public void run() {
                    levelDone=false;
                    updateDisplay();
                }
            }, 200);



       }
    }

    public void levelEnd(){
       levelDone=true;
       updateDisplay();
       return;
    }

    private void updateDisplay(){
        mLevel.setText(String.valueOf(level));
        mScore.setText(String.valueOf(score));
        if (level >= 1) {

            if (levelDone) {
                button.setText(String.valueOf("Next Level"));
            } else {
                button.setText(String.valueOf(""));
            }
        }
        if (isGameOver){
            button.setText(String.valueOf("New Game"));
        }
    }


    public void goButtonClickHandler(View view){
        mSoundHelper.playClick();

        if (levelDone){
                  levelStart();

        } else {
            return;
        }

    }


}

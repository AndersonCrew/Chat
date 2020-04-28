package com.dazone.crewchatoff.customs;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dazone.crewchatoff.R;

/**
 * Created by maidinh on 27-Sep-17.
 */

public class AudioPlayer extends Dialog {
    private String TAG = "AudioPlayer";
    private MediaPlayer mp;
    private ProgressBar songProgressBar;
    private Handler mHandler = new Handler();
    private TextView songTotalDurationLabel, songCurrentDurationLabel, tvName;

    public AudioPlayer(@NonNull Context context, String pathAudio, String _fileName) {
        super(context);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.player);

        tvName = findViewById(R.id.tvName);
        if (_fileName == null) _fileName = "";
        tvName.setText(_fileName);

        songProgressBar = findViewById(R.id.songProgressBar);

        songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel);
        songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel);
        mp = new MediaPlayer();
        try {
            mp.setDataSource(pathAudio);
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    dismiss();
                }
            });
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            updateProgressBar();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mp == null) return;

        FrameLayout btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        onDestroy();
    }

    void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateTimeTask);
        }
        if (mp != null) {
            mp.release();
        }
        mUpdateTimeTask = null;
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = getProgressPercentage(currentDuration, totalDuration);
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);
            Log.d(TAG, "mUpdateTimeTask");
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }
}

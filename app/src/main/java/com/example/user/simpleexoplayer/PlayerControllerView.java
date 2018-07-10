package com.example.user.simpleexoplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

public class PlayerControllerView extends FrameLayout {

    private SimpleExoPlayer mPlayer;
    private Context mContext;
    private ViewGroup mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mShowing;
    private boolean mDragging;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS =2;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private ImageButton mPauseButton;
    private ImageButton mPlayButton;

    private Handler mHandler = new MessageHandler(this);


    public PlayerControllerView(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public PlayerControllerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    public void setAnchorView(ViewGroup view){
        mAnchor = view;
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        removeAllViews();
        View v = makeControllerView();
        addView(v,frameParams);
    }
    public void setMediaPlayer(SimpleExoPlayer player) {
        mPlayer = player;
        updatePausePlay();
        //updateFullScreen();
    }
    protected View makeControllerView(){
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.player_controller,null);
        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton =(ImageButton) v.findViewById(R.id.pause);
        if(mPauseButton != null){
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mProgress = (ProgressBar)v.findViewById(R.id.mediacontroller_progress);
        if(mProgress != null){
            if(mProgress instanceof SeekBar){
                SeekBar seeker = (SeekBar)mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax((int)(1000*mPlayer.getDuration()));
        }

        mEndTime = (TextView)v.findViewById(R.id.time);
        mCurrentTime = (TextView)v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    public void show(){
        show(sDefaultTimeout);
    }


    public void show(int timeout){
        if(!mShowing && mAnchor != null){
            setProgress();
            if(mPauseButton != null){
                mPauseButton.requestFocus();
            }

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );
            mAnchor.addView(this,tlp);
            mShowing = true;
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if(timeout !=0){
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg,timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    private void hide() {
        if(mAnchor == null){
            return;
        }
        mAnchor.removeView(this);
        mHandler.removeMessages(SHOW_PROGRESS);
        mShowing = false;
    }
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = (int)mPlayer.getCurrentPosition();
        int duration = (int)mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferedPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event){
        show(sDefaultTimeout);
        return false;
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.getPlayWhenReady()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }

        if (mPlayer.getPlayWhenReady()) {
            mPlayer.setPlayWhenReady(false);
        } else {
            mPlayer.setPlayWhenReady(true);
        }
        updatePausePlay();
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;
            mHandler.removeMessages(SHOW_PROGRESS);
        }public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mPlayer == null) {
                return;
            }
            if (!fromuser) {
                return;
            }
            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    private static class MessageHandler extends Handler {
        private final WeakReference<PlayerControllerView> mView;

        MessageHandler(PlayerControllerView view) {
            mView = new WeakReference<PlayerControllerView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            PlayerControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.getPlayWhenReady()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }


}

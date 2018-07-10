package com.example.user.simpleexoplayer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;


import com.google.android.exoplayer2.ExoPlayerFactory;

import com.google.android.exoplayer2.SimpleExoPlayer;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;

import com.google.android.exoplayer2.source.MediaSource;

import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;

import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback{

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private PlayerControllerView controller;

    private SimpleExoPlayer player;
    private com.google.android.exoplayer2.upstream.DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private BandwidthMeter bandwidthMeter;
    //private MediaCodecVideoRenderer videoRenderer;
    //LoopingMediaSource loopingSource;
    private boolean autoPlay = true;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mSurfaceView = findViewById(R.id.videoSurface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "simpleexoplayer"),
                (TransferListener<? super com.google.android.exoplayer2.upstream.DataSource>)bandwidthMeter);
        controller = new PlayerControllerView(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }


    private void initializePlayer(){
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
        //player.setPlayWhenReady(true);
        DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("http://neonplayer.api.dev.naver.com/file/pdtest/720p_sample.mp4"),
                mediaDataSourceFactory, extractorsFactory, null, null);

        controller.setMediaPlayer(player);
        controller.setAnchorView((FrameLayout)findViewById(R.id.videoSurfaceContainer));
        player.setVideoSurfaceView(mSurfaceView);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);
    }


    private void maybeStartPlayback() {
    //    player.setVideoSurfaceHolder(mSurfaceHolder);
      //  if (autoPlay) {
        //    player.setPlayWhenReady(true);
          //  autoPlay = false;
     //   }
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        maybeStartPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

}

package com.posseggs.motionstream;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class Stream extends Fragment {

    private SurfaceHolder holder;
    private LibVLC libvlc = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;

    public final static String TAG = "Stream";

    SurfaceView surface;

    private ProgressDialog progress = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        //Make instance stay active when main activity dies
        setRetainInstance(true);
        surface = view.findViewById(R.id.surface);
        holder = surface.getHolder();
        holder.setSizeFromLayout();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //if (MainActivity.video.getAutoplay())
        //if (mMediaPlayer == null)
            startStream(); //Start stream if autoplay is enabled
        //else
        //    restartPlayer();
    }

    /*private void setSize(int width, int height)
    {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if (holder == null || surface == null)
            return;

        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        holder.setFixedSize(mVideoWidth, mVideoHeight);
        ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        surface.setLayoutParams(lp);
        surface.invalidate();
    }
    */

    //Functions for stream
    public void startStream()
    {
        try
        {
            //Create the player to show stream
            createPlayer(MainActivity.video.getUri());
        }
        catch (Exception ex)
        {
            Log.d(TAG,ex.getMessage());
            //Toast.makeText(this, ex.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void restartPlayer()
    {
        Uri media = MainActivity.video.getUri();
        // Creating media player
        //mMediaPlayer = new MediaPlayer(libvlc);
        //mMediaPlayer.setEventListener(mPlayerListener);

        // Setting up video output
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setVideoView(surface);
        //vout.addCallback(this);
        vout.attachViews();

        Media m = new Media(libvlc, media);
        mMediaPlayer.setMedia(m);
        mMediaPlayer.play();
    }

    private void createPlayer(Uri media)
    {
        //Delete player if exists
        releasePlayer();
        try
        {
            // Create LibVLC
            ArrayList<String> options = new ArrayList<>();

            //Options for stream
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add(":network-caching=100");
            options.add(":clock-jitter=0");
            options.add("clock-synchro=0");

            libvlc = new LibVLC(getActivity(), options);
            holder = surface.getHolder();
            holder.setKeepScreenOn(true);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);

            // Setting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surface);
            //vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, media);
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        }
        catch (Exception e)
        {
            Log.d(TAG,e.getMessage());
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        //vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }


    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);

    /*
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen)
    {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }
    */

    private class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<Stream> mOwner;

        public MyPlayerListener(Stream owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    break;
                case MediaPlayer.Event.Playing:
                    Log.d(TAG,"Playing stream");
                    if (MainActivity.progress != null)
                        MainActivity.progress.dismiss(); //Close loading dialog
                    break;
                case MediaPlayer.Event.Paused:
                    Log.d(TAG,"Paused stream");
                    break;
                case MediaPlayer.Event.Stopped:
                    Log.d(TAG,"Stopped stream");
                    break;
                default:
                    break;
            }
        }
    }
}

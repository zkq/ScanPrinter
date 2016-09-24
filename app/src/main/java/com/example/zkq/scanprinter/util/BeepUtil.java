package com.example.zkq.scanprinter.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.zkq.scanprinter.R;

import java.io.IOException;

/**
 * Created by zkq on 2016/9/24.
 */

public class BeepUtil {
    String TAG = BeepUtil.class.getSimpleName();

    private static MediaPlayer mediaPlayer;


    public static void beep(Context context)
    {
        buildMediaplayer(context);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int vol = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int volMax = manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

        if(mediaPlayer != null)
        {
            mediaPlayer.setVolume(vol * 1.0f /volMax, vol * 1.0f /volMax);
            mediaPlayer.start();
        }
    }




    private static void buildMediaplayer(final Context context)
    {
        if(mediaPlayer != null)
        {
            return;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });

        AssetFileDescriptor descriptor = context.getResources().openRawResourceFd(R.raw.beep);


        try {
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}

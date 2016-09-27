package joe.com.mediasessioncompatsample;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

/**
 * Description
 * Created by chenqiao on 2016/9/24.
 */

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private MusicBinder binder = new MusicBinder();

    private MediaSessionCompat mediaSessionCompat;
    private String[] mp3s = new String[]{"http://yinyueshiting.baidu.com/data2/music/49d23c00a69f913ff41bbf0f4ef9af44/261813937/261813937.mp3?xcode=3c8806142c92ccd68e84979b09d702df",
            "http://yinyueshiting.baidu.com/data2/music/a91a082fa3b5f11553c4343002ff54b7/269187740/269187740.mp3?xcode=3c8806142c92ccd68e84979b09d702df",
            "http://yinyueshiting.baidu.com/data2/music/4708c0bda6938501e19d89b186573f13/268028134/268028134.mp3?xcode=3c8806142c92ccd68e84979b09d702df"};

    private int index = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        ComponentName mediaButtonReceiver = new ComponentName(this, MediaButtonReceiver.class);
        mediaSessionCompat = new MediaSessionCompat(this, getClass().getName(), mediaButtonReceiver, null);
        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSessionCompat.setCallback(callback);
        mediaSessionCompat.setActive(true);
        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f).build();
        mediaSessionCompat.setPlaybackState(playbackStateCompat);
        initMediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        mediaSessionCompat.release();
    }

    private void initMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build();
                    mediaSessionCompat.setPlaybackState(playbackStateCompat);
                    //TODO 和setMediaPlayBack相同，使用setMetadata传送歌曲相关信息，然后在activity中的onMetaChanged中接收，更新UI。
                    mediaSessionCompat.setMetadata(
                            new MediaMetadataCompat.Builder().
                                    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration()).
                                    build());
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f).build();
                    mediaSessionCompat.setPlaybackState(playbackStateCompat);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_ERROR, 0, 1.0f).build();
                    mediaSessionCompat.setPlaybackState(playbackStateCompat);
                    reset();
                    return true;
                }
            });
        }
    }

    private void play() {
        reset();
        if (mediaPlayer != null && index < mp3s.length && index >= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mediaPlayer.setDataSource(mp3s[index]);
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build();
            mediaSessionCompat.setPlaybackState(playbackStateCompat);
        }
    }

    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1.0f).build();
            mediaSessionCompat.setPlaybackState(playbackStateCompat);
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f).build();
            mediaSessionCompat.setPlaybackState(playbackStateCompat);
        }
    }

    private void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
    }

    private void release() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public class MusicBinder extends Binder {
        public MediaSessionCompat getMediaSession() {
            return MusicService.this.mediaSessionCompat;
        }
    }


    private MediaSessionCompat.Callback callback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
            Log.d("MusicService", "onPrepare: ");
        }

        @Override
        public void onPlay() {
            super.onPlay();
            if (mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                Log.d("MusicService", "onPlay: resume");
                resume();
            } else {
                Log.d("MusicService", "onPlay: play");
                play();
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            Log.d("MusicService", "onPlayFromMediaId: ");
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            Log.d("MusicService", "onPlayFromUri: ");
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d("MusicService", "onPause: ");
            pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.d("MusicService", "onSkipToNext: ");
            index = (index + 1) == mp3s.length ? 0 : (index + 1);
            stop();
            play();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.d("MusicService", "onSkipToPrevious: ");
            index = (index - 1) < 0 ? mp3s.length - 1 : (index - 1);
            stop();
            play();
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d("MusicService", "onStop: ");
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
        }
    };
}
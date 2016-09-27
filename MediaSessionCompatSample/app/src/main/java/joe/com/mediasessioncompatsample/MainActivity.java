package joe.com.mediasessioncompatsample;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView nameTv;
    private ImageView playBtn, preBtn, nextBtn;
    private SeekBar seekBar;
    private ScheduledExecutorService progressUpdateThread;
    private Handler handler = new Handler();

    private long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);
        progressUpdateThread = Executors.newSingleThreadScheduledExecutor();
        progressUpdateThread.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mediaControllerCompat != null) {
                    PlaybackStateCompat playbackState = mediaControllerCompat.getPlaybackState();
                    if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                        final long currentPosition = (long) (((SystemClock.elapsedRealtime() - playbackState.getLastPositionUpdateTime()) * playbackState.getPlaybackSpeed())
                                + playbackState.getPosition());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                int progress = 0;
                                if (duration > 0) {
                                    progress = (int) (currentPosition * 100 / duration);
                                }
                                seekBar.setProgress(progress);
                            }
                        });
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void initViews() {
        nameTv = (TextView) findViewById(R.id.tv_name);
        playBtn = (ImageView) findViewById(R.id.btn_play_pause);
        preBtn = (ImageView) findViewById(R.id.btn_pre);
        nextBtn = (ImageView) findViewById(R.id.btn_next);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(100);
        seekBar.setProgress(0);
        playBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }

    private MediaSessionCompat sessionCompat;
    private MediaSessionCompat.Token token;
    private MediaControllerCompat mediaControllerCompat;
    private MediaControllerCompat.TransportControls transportControls;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sessionCompat = ((MusicService.MusicBinder) service).getMediaSession();
            token = sessionCompat.getSessionToken();
            try {
                mediaControllerCompat = new MediaControllerCompat(MainActivity.this, token);
                mediaControllerCompat.registerCallback(callback);
                transportControls = mediaControllerCompat.getTransportControls();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            Log.d("MainActivity", "onPlaybackStateChanged: " + state.toString());
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                playBtn.setImageResource(android.R.drawable.ic_media_play);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }

        @Override
        public void binderDied() {
            super.binderDied();
        }
    };

    @Override
    public void onClick(View v) {
        if (transportControls == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_play_pause:
                Log.d("MainActivity", "onClick: " + mediaControllerCompat.getPlaybackState().toString());
                if (mediaControllerCompat.getPlaybackState().getState() != PlaybackStateCompat.STATE_PLAYING) {
                    transportControls.play();
                } else {
                    transportControls.pause();
                }
                break;
            case R.id.btn_next:
                transportControls.skipToNext();
                break;
            case R.id.btn_pre:
                transportControls.skipToPrevious();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
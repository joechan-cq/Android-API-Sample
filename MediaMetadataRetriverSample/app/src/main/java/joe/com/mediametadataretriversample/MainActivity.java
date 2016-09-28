package joe.com.mediametadataretriversample;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private TextView titleTv, albumTv, artistTv, mimeTypeTv, durationTv;
    private File musicFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //copy music from assets to storage
                try {
                    InputStream inputStream = getAssets().open("music.mp3");
                    musicFile = new File(getExternalCacheDir(), "music.mp3");
                    if (!musicFile.exists()) {
                        musicFile.createNewFile();
                    }
                    FileOutputStream outputStream = new FileOutputStream(musicFile);
                    byte[] temp = new byte[1024];
                    int len;
                    while ((len = inputStream.read(temp)) != -1) {
                        outputStream.write(temp, 0, len);
                    }
                    outputStream.flush();
                    inputStream.close();
                    outputStream.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "资源copy完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        titleTv = (TextView) findViewById(R.id.tv_title);
        albumTv = (TextView) findViewById(R.id.tv_album);
        mimeTypeTv = (TextView) findViewById(R.id.tv_mime);
        artistTv = (TextView) findViewById(R.id.tv_artist);
        durationTv = (TextView) findViewById(R.id.tv_duration);
        findViewById(R.id.btn_read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(musicFile.getAbsolutePath());
                titleTv.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                albumTv.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                mimeTypeTv.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
                artistTv.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                durationTv.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                //如果是视频文件，可以使用getFrameAtTime获取某一时间的视频截图，不过高清视频可能会OOM
//              Bitmap bitmap=retriever.getFrameAtTime(time);
            }
        });
    }
}

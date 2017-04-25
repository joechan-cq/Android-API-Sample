package joe.com.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by joe on 2017/4/25.
 */
public class PictureActivity extends AppCompatActivity {
    private ImageView img;
    public static byte[] data;
    public static int orientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture);
        img = (ImageView) findViewById(R.id.img);
        if (data == null) {
            Toast.makeText(this, "数据不正确", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        img.post(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageBitmap(bmp);
                            }
                        });
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(PictureActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }
    }
}

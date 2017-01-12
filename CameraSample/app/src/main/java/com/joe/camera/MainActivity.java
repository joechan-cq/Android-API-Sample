package com.joe.camera;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private TextView translateXTv, translateYTv, translateZTv, skewXTv, skewYTv, rotateXTv, rotateYTv, rotateZTv;

    private ImageView imageView;

    private Camera camera;

    private int translateX = 0;
    private int translateY = 0;
    private int translateZ = 0;
    private int rotateX = 0;
    private int rotateY = 0;
    private int rotateZ = 0;
    private float skewX = 0;
    private float skewY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateXTv = (TextView) findViewById(R.id.tv_rotate_x);
        rotateYTv = (TextView) findViewById(R.id.tv_rotate_y);
        rotateZTv = (TextView) findViewById(R.id.tv_rotate_z);
        skewXTv = (TextView) findViewById(R.id.tv_skew_x);
        skewYTv = (TextView) findViewById(R.id.tv_skew_y);
        translateXTv = (TextView) findViewById(R.id.tv_translate_x);
        translateYTv = (TextView) findViewById(R.id.tv_translate_y);
        translateZTv = (TextView) findViewById(R.id.tv_translate_z);

        imageView = (ImageView) findViewById(R.id.img);

        SeekBar rx = (SeekBar) findViewById(R.id.sb_rotate_x);
        SeekBar ry = (SeekBar) findViewById(R.id.sb_rotate_y);
        SeekBar rz = (SeekBar) findViewById(R.id.sb_rotate_z);
        SeekBar tx = (SeekBar) findViewById(R.id.sb_translate_x);
        SeekBar ty = (SeekBar) findViewById(R.id.sb_translate_y);
        SeekBar tz = (SeekBar) findViewById(R.id.sb_translate_z);
        SeekBar sx = (SeekBar) findViewById(R.id.sb_skew_x);
        SeekBar sy = (SeekBar) findViewById(R.id.sb_skew_y);

        rx.setOnSeekBarChangeListener(this);
        ry.setOnSeekBarChangeListener(this);
        rz.setOnSeekBarChangeListener(this);
        tx.setOnSeekBarChangeListener(this);
        ty.setOnSeekBarChangeListener(this);
        tz.setOnSeekBarChangeListener(this);
        sx.setOnSeekBarChangeListener(this);
        sy.setOnSeekBarChangeListener(this);

        camera = new Camera();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_rotate_x:
                    rotateX = progress;
                    rotateXTv.setText(String.valueOf(rotateX));
                    break;
                case R.id.sb_rotate_y:
                    rotateY = progress;
                    rotateYTv.setText(String.valueOf(rotateY));
                    break;
                case R.id.sb_rotate_z:
                    rotateZ = progress;
                    rotateZTv.setText(String.valueOf(rotateZ));
                    break;
                case R.id.sb_translate_x:
                    translateX = progress - 100;
                    translateXTv.setText(String.valueOf(translateX));
                    break;
                case R.id.sb_translate_y:
                    translateY = progress - 100;
                    translateYTv.setText(String.valueOf(translateY));
                    break;
                case R.id.sb_translate_z:
                    translateZ = progress - 100;
                    translateZTv.setText(String.valueOf(translateZ));
                    break;
                case R.id.sb_skew_x:
                    skewX = (progress - 100) / 100f;
                    skewXTv.setText(String.valueOf(skewX));
                    break;
                case R.id.sb_skew_y:
                    skewY = (progress - 100) / 100f;
                    skewYTv.setText(String.valueOf(skewY));
                    break;
            }
            refreshImage();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void refreshImage() {
        // 获取待处理的图像
        BitmapDrawable tmpBitDra = (BitmapDrawable) getResources().getDrawable(R.drawable.test);
        Bitmap tmpBit = tmpBitDra.getBitmap();
        // 开始处理图像
        // 1.获取处理矩阵
        // 记录一下初始状态。save()和restore()可以将图像过渡得柔和一些。
        // Each save should be balanced with a call to restore().
        camera.save();
        Matrix matrix = new Matrix();
        // rotate
        camera.rotateX(rotateX);
        camera.rotateY(rotateY);
        camera.rotateZ(rotateZ);
        // translate
        camera.translate(translateX, translateY, translateZ);
        camera.getMatrix(matrix);
        // 恢复到之前的初始状态。
        camera.restore();

        matrix.preSkew(skewX, skewY);
        Bitmap newBit = null;
        try {
            // 经过矩阵转换后的图像宽高有可能不大于0，此时会抛出IllegalArgumentException
            newBit = Bitmap.createBitmap(tmpBit, 0, 0, tmpBit.getWidth(), tmpBit.getHeight(), matrix, true);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        if (newBit != null) {
            imageView.setImageBitmap(newBit);
        }
    }

}

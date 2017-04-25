package joe.com.camerademo;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Camera老API示例代码
 * Created by joe on 2017/4/25.
 */
public class CameraActivity extends AppCompatActivity {

    private int cameraId;
    private Camera camera;

    private TextureView preView;
    private SurfaceTexture mSurfaceTexture;
    private OrientationEventListener orientationEventListener;

    private int currentOrientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camera);
        initUI();

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            Toast.makeText(this, "未找到摄像头", Toast.LENGTH_SHORT).show();
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        //传感器
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                currentOrientation = orientation;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        orientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
        orientationEventListener.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    private void initUI() {
        preView = (TextureView) findViewById(R.id.preview);
        preView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
                initCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
        findViewById(R.id.btn_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        findViewById(R.id.btn_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    /**
     * 初始化Camera
     */
    private void initCamera() {
        try {
            camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            //设置闪光灯
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            //设置对焦模式
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //根据屏幕旋转角度调整预览角度
            CameraUtils.setCameraDisplayOrientation(this, cameraId, camera);

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            Camera.Size size = CameraUtils.getMostSuitablePreviewSize(camera, screenHeight, screenWidth);
            parameters.setPreviewSize(size.width, size.height);
            Log.d("CameraActivity", "preSize: " + size.width + "  " + size.height);

            parameters.setPictureFormat(ImageFormat.JPEG);
            Camera.Size picSize = CameraUtils.getMostSuitablePictureSize(camera, size.width, size.height);
            parameters.setPictureSize(picSize.width, picSize.height);
            Log.d("CameraActivity", "picSize: " + picSize.width + "  " + picSize.height);

            camera.setParameters(parameters);
            camera.startPreview();
            try {
                camera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            Toast.makeText(this, "打开摄像头出错，被占用或其他", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 切换前后摄像头
     */
    private void switchCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        releaseCamera();
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        initCamera();
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(CameraUtils.correctOrientation(cameraId, currentOrientation));
            camera.setParameters(parameters);
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    Toast.makeText(CameraActivity.this, "正在拍照", Toast.LENGTH_SHORT).show();
                }
            }, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] jpegData, Camera camera) {
                    PictureActivity.data = jpegData;
                    PictureActivity.orientation = currentOrientation;
                    Intent intent = new Intent(CameraActivity.this, PictureActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}

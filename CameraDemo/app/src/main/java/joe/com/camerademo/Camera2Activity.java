package joe.com.camerademo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Camera2Api使用示例
 * Created by joe on 2017/4/25.
 */
public class Camera2Activity extends AppCompatActivity {

    private CameraManager mCameraManager;
    private CameraDevice mCamera;
    private CaptureRequest.Builder previewRequestBuilder, captureRequestBuilder;
    private ImageReader imageReader;
    private CameraCaptureSession mCaptureSession;

    private TextureView preView;
    private SurfaceTexture mSurfaceTexture;

    private String mCameraId;
    private String[] mCameraIds = new String[2];
    private HandlerThread asyncThread;
    private Handler asyncHandler;
    private int screenWidth;
    private int screenHeight;
    private Size mPreviewSize, mPictureSize;
    private CameraCharacteristics cameraCharacteristics;
    private Integer mSensorOrientation;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this, "新api要求Android 5.0以上", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                String[] cameraIds = mCameraManager.getCameraIdList();
                if (cameraIds.length == 0) {
                    Toast.makeText(this, "未找到相机设备", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    for (String cameraId : cameraIds) {
                        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                        Integer type = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                        if (type != null) {
                            //将后置摄像头放到0中，前置放到1中
                            if (CameraCharacteristics.LENS_FACING_BACK == type) {
                                mCameraIds[0] = cameraId;
                            } else if (CameraCharacteristics.LENS_FACING_FRONT == type) {
                                mCameraIds[1] = cameraId;
                            }
                        }
                    }
                    mCameraId = mCameraIds[0];
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        setContentView(R.layout.camera);
        initUI();
        asyncThread = new HandlerThread("Camera");
        asyncThread.start();
        asyncHandler = new Handler(asyncThread.getLooper());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            releaseCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncThread != null) {
            asyncThread.quit();
            asyncThread = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            releaseCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initUI() {
        preView = (TextureView) findViewById(R.id.preview);
        preView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceTexture = surface;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    initCamera();
                }
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
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        findViewById(R.id.btn_take).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePicture() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        //获取传感器的角度
        mSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        if (mSensorOrientation == null) {
            mSensorOrientation = 0;
        }
        //修正角度
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, CameraUtils.getOrientation(mSensorOrientation,
                rotation));

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();//1
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                image.close();//2，一定要close

                PictureActivity.data = bytes;
                Intent intent = new Intent(Camera2Activity.this, PictureActivity.class);
                startActivity(intent);
            }
        }, asyncHandler);
        try {
            //拍照
            mCaptureSession.capture(captureRequestBuilder.build(), null, asyncHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void switchCamera() {
        if (mCameraIds[0] != null && mCameraIds[1] != null) {
            releaseCamera();
            if (mCameraId.equals(mCameraIds[0])) {
                mCameraId = mCameraIds[1];
            } else if (mCameraId.equals(mCameraIds[1])) {
                mCameraId = mCameraIds[0];
            }
            initCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera() {
        if (mCameraManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager
                    .PERMISSION_GRANTED) {
                return;
            }
            try {
                cameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
                //打开相机
                mCameraManager.openCamera(mCameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        mCamera = camera;
                        startPreview();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {

                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {

                    }
                }, asyncHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPreview() {
        try {
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics
                    .SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                //获取支持的预览尺寸（但和Camera API 的getSupportPreviewSize结果不一样）
                Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
                mPreviewSize = CameraUtils.getMostSuitablePreviewSize(previewSizes, screenHeight, screenWidth);
                Log.d("Camera2Activity", "previewSize: " + mPreviewSize.getWidth() + " " + mPreviewSize.getHeight());
                mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                //根据输出类型获取支持的尺寸
                Size[] outputSizes = map.getOutputSizes(ImageFormat.JPEG);
                mPictureSize = CameraUtils.getMostSuitablePictureSize(outputSizes, screenHeight, screenWidth, false);
                Log.d("Camera2Activity", "pictureSize: " + mPictureSize.getWidth() + " " + mPictureSize.getHeight());
                imageReader = ImageReader.newInstance(mPictureSize.getWidth(), mPictureSize.getHeight(), ImageFormat
                        .JPEG, 1);
            }

            previewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            Surface previewSurface = new Surface(mSurfaceTexture);
            previewRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            mCamera.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()), new
                    CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                                    .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest
                                    .CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            try {
                                //进行预览
                                session.setRepeatingRequest(previewRequestBuilder.build(), null, asyncHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, asyncHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void releaseCamera() {
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (mCaptureSession != null) {
            try {
                mCaptureSession.stopRepeating();
                mCaptureSession.abortCaptures();
                mCaptureSession.close();
                mCamera.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCaptureSession = null;
        }
    }
}

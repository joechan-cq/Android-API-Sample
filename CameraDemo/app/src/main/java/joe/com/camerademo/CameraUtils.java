package joe.com.camerademo;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import java.util.List;

/**
 * Camera工具类
 * Created by joe on 2017/4/25.
 */
public class CameraUtils {
    /**
     * 修正camera预览的角度问题，代码出自{@link Camera#setDisplayOrientation(int)}注释
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 获取最适合的预览尺寸
     *
     * @param camera         Camera实例
     * @param preview_width  期望的预览宽度
     * @param preview_height 期望的预览高度
     * @return 最适合的尺寸
     */
    public static Camera.Size getMostSuitablePreviewSize(Camera camera, int preview_width, int preview_height) {
        Camera.Size result = null;
        Camera.Parameters parameters = camera.getParameters();
        float preview_proportion = preview_width / (preview_height * 1f);
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        float proportion;
        for (Camera.Size size : sizes) {
            Log.d("CameraUtils", "supportPreviewSize: " + size.width + "*" + size.height);
            proportion = size.width / (size.height * 1f);
            if (preview_proportion == proportion) {
                if (size.width >= preview_width && size.height >= preview_height) {
                    if (result != null) {
                        if (result.width < size.width) {
                            result = size;
                        }
                    } else {
                        result = size;
                    }
                }
            }
        }
        if (result == null) {
            //没有找到比例相等的previewSize，那就找比例最接近的previewSize
            float minOffset = Float.MAX_VALUE;
            for (Camera.Size size : sizes) {
                proportion = size.width / (size.height * 1f);
                if (size.width >= preview_width && size.height >= preview_height) {
                    if (minOffset > Math.abs(preview_proportion - proportion)) {
                        if (result != null) {
                            if (result.width < size.width) {
                                result = size;
                            }
                        } else {
                            result = size;
                        }
                        minOffset = Math.abs(preview_proportion - proportion);
                    }
                }
            }
        }
        return result;
    }

    public static Camera.Size getMostSuitablePictureSize(Camera camera, int preview_width, int preview_height) {
        return getMostSuitablePictureSize(camera, preview_width, preview_height, false);
    }

    /**
     * 找到与preview最适合的pictureSize
     *
     * @param camera         camera实例
     * @param preview_width  preview宽度
     * @param preview_height preview高度
     * @param findMax        是否寻找比例相同且尺寸最大的
     * @return 最合适的尺寸
     */
    public static Camera.Size getMostSuitablePictureSize(Camera camera, int preview_width, int preview_height,
                                                         boolean findMax) {
        Camera.Size result = null;
        Camera.Parameters parameters = camera.getParameters();
        float preview_proportion = preview_width / (preview_height * 1f);
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        float proportion;
        for (Camera.Size size : sizes) {
            Log.d("CameraUtils", "supportPictureSize: " + size.width + "*" + size.height);
            proportion = size.width / (size.height * 1f);
            if (size.width >= preview_width && size.height >= preview_height) {
                if (preview_proportion == proportion) {
                    if (result != null) {
                        if (result.width < size.width) {
                            result = size;
                        }
                    } else {
                        result = size;
                        if (!findMax) {
                            break;
                        }
                    }
                }
            }
        }
        if (result == null) {
            //没有找到比例相等的pictureSize，那就找比例最接近的pictureSize
            float minOffset = Float.MAX_VALUE;
            for (Camera.Size size : sizes) {
                proportion = size.width / (size.height * 1f);
                if (size.width >= preview_width && size.height >= preview_height) {
                    if (minOffset > Math.abs(preview_proportion - proportion)) {
                        if (result != null) {
                            if (result.width < size.width) {
                                result = size;
                            }
                        } else {
                            result = size;
                        }
                        minOffset = Math.abs(preview_proportion - proportion);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 调整Camera输出的图像角度,该代码出自{@link android.hardware.Camera.Parameters#setRotation(int)}上的注释
     *
     * @param cameraId    cameraId
     * @param orientation 传感器输出的角度值
     * @return 修正后的角度
     */
    public static int correctOrientation(int cameraId, int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        orientation = (orientation + 45) / 90 * 90;
        int rotation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        return rotation;
    }
}

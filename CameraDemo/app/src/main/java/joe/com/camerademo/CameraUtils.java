package joe.com.camerademo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
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
            Log.d("CameraUtils", "supportPreviewSize: " + size.width + "*" + size.height + " " + (size.width / (1f *
                    size.height)));
        }
        for (Camera.Size size : sizes) {
            proportion = size.width / (size.height * 1f);
            if (preview_proportion == proportion) {
                if (size.width >= preview_width / 2 && size.height >= preview_height / 2) {
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
            if (result == null) {
                for (Camera.Size size : sizes) {
                    proportion = size.width / (size.height * 1f);
                    if (size.width >= preview_width / 2 && size.height >= preview_height / 2) {
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
            Log.d("CameraUtils", "supportPictureSize: " + size.width + "*" + size.height + " " + (size.width / (1f *
                    size.height)));
        }
        for (Camera.Size size : sizes) {
            proportion = size.width / (size.height * 1f);
            if (size.width >= preview_width / 2 && size.height >= preview_height / 2) {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Size getMostSuitablePreviewSize(Size[] supportSizes, int preview_width, int preview_height) {
        float preview_proportion = preview_width / (1f * preview_height);
        Size result = null;
        float proportion;
        for (Size size : supportSizes) {
            Log.d("CameraUtils", "supportPreviewSize: " + size.getWidth() + " " + size.getHeight());
        }
        for (Size size : supportSizes) {
            proportion = size.getWidth() / (size.getHeight() * 1f);
            if (preview_proportion == proportion) {
                if (size.getWidth() >= preview_width / 2 && size.getHeight() >= preview_height / 2) {
                    if (result != null) {
                        if (result.getWidth() < size.getWidth()) {
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
            for (Size size : supportSizes) {
                proportion = size.getWidth() / (size.getHeight() * 1f);
                if (size.getWidth() >= preview_width && size.getHeight() >= preview_height) {
                    if (minOffset > Math.abs(preview_proportion - proportion)) {
                        if (result != null) {
                            if (result.getWidth() < size.getWidth()) {
                                result = size;
                            }
                        } else {
                            result = size;
                        }
                        minOffset = Math.abs(preview_proportion - proportion);
                    }
                }
            }
            if (result == null) {
                for (Size size : supportSizes) {
                    proportion = size.getWidth() / (size.getHeight() * 1f);
                    if (size.getWidth() >= preview_width / 2 && size.getHeight() >= preview_height / 2) {
                        if (minOffset > Math.abs(preview_proportion - proportion)) {
                            if (result != null) {
                                if (result.getWidth() < size.getWidth()) {
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
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Size getMostSuitablePictureSize(Size[] supportSizes, int expectWidth, int expectHeight, boolean
            findMax) {
        Size result = null;
        float preview_proportion, proportion;
        preview_proportion = expectWidth / (1f * expectHeight);
        for (Size size : supportSizes) {
            Log.d("CameraUtils", "supportPictureSize: " + size.getWidth() + "*" + size.getHeight() + " " + (size
                    .getWidth() / (1f * size.getHeight())));
        }
        for (Size size : supportSizes) {
            proportion = size.getWidth() / (size.getHeight() * 1f);
            if (preview_proportion == proportion) {
                if (size.getWidth() >= expectWidth / 2 && size.getHeight() >= expectHeight / 2) {
                    if (result != null) {
                        if (result.getWidth() < size.getWidth()) {
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
            for (Size size : supportSizes) {
                proportion = size.getWidth() / (size.getHeight() * 1f);
                if (size.getWidth() >= expectWidth && size.getHeight() >= expectHeight) {
                    if (minOffset > Math.abs(preview_proportion - proportion)) {
                        if (result != null) {
                            if (result.getWidth() < size.getWidth()) {
                                result = size;
                            }
                        } else {
                            result = size;
                            if (!findMax) {
                                break;
                            }
                        }
                        minOffset = Math.abs(preview_proportion - proportion);
                    }
                }
            }
            if (result == null) {
                for (Size size : supportSizes) {
                    proportion = size.getWidth() / (size.getHeight() * 1f);
                    if (size.getWidth() >= expectWidth / 2 && size.getHeight() >= expectHeight / 2) {
                        if (minOffset > Math.abs(preview_proportion - proportion)) {
                            if (result != null) {
                                if (result.getWidth() < size.getWidth()) {
                                    result = size;
                                }
                            } else {
                                result = size;
                                if (!findMax) {
                                    break;
                                }
                            }
                            minOffset = Math.abs(preview_proportion - proportion);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static int getOrientation(int mSensorOrientation, int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }
}

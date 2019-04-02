package com.example.apriltagstest_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.Arrays;
import java.util.Collections;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2View extends TextureView {
    private static final String                 TAG         = "Camera2View";
    private              Context                mContext; // 声明一个上下文对象
    private              Handler                mHandler;
    private              HandlerThread          mThreadHandler;
    private              CaptureRequest.Builder mPreviewBuilder; // 声明一个拍照请求构建器对象
    private              CameraCaptureSession   mCameraSession; // 声明一个相机拍照会话对象
    private              CameraDevice           mCameraDevice; // 声明一个相机设备对象
    private              ImageReader            mImageReader; // 声明一个图像读取器对象
    private              Size                   mPreViewSize; // 预览画面的尺寸
    private              int                    mCameraType = CameraCharacteristics.LENS_FACING_FRONT; // 摄像头类型

    private SurfaceView   overlayView;
    private SurfaceHolder surfaceHolder;

    public Camera2View(Context context) {
        this(context, null);
    }

    public Camera2View(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mThreadHandler = new HandlerThread("camera2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
    }

    // 打开指定摄像头的相机视图
    public void open(int camera_type) {
        mCameraType = camera_type;
        // 设置表面纹理变更监听器
        setSurfaceTextureListener(mSurfacetextlistener);
    }

    // 打开相机
    private void openCamera() {
        // 从系统服务中获取相机管理器
        CameraManager cm = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        String cameraid = mCameraType + "";

        overlayView = new SurfaceView(mContext);
        overlayView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        overlayView.setZOrderOnTop(true);//处于顶层
        surfaceHolder = overlayView.getHolder();

        try {
            // 获取可用相机设备列表
            CameraCharacteristics cc = cm.getCameraCharacteristics(cameraid);
            // 检查相机硬件的支持级别
            // CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL表示完全支持
            // CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED表示有限支持
            // CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY表示遗留的
            int level = cc.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
            // 获取预览画面的尺寸
            mPreViewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            // 创建一个JPEG格式的图像读取器
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 10);
            // 设置图像读取器的图像可用监听器，一旦捕捉到图像数据就会触发监听器的onImageAvailable方法
            mImageReader.setOnImageAvailableListener(onImageAvaiableListener, mHandler);
            // 开启摄像头
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cm.openCamera(cameraid, mDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 关闭相机
    private void closeCamera() {
        if (null != mCameraSession) {
            mCameraSession.close(); // 关闭相机拍摄会话
            mCameraSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close(); // 关闭相机设备
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close(); // 关闭图像读取器
            mImageReader = null;
        }
    }

    // 定义一个表面纹理变更监听器。TextureView准备就绪后，立即开启相机
    private SurfaceTextureListener mSurfacetextlistener = new SurfaceTextureListener() {
        // 在纹理表面可用时触发
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(); // 打开相机
        }

        // 在纹理表面的尺寸发生改变时触发
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        // 在纹理表面销毁时触发
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera(); // 关闭相机
            return true;
        }

        // 在纹理表面更新时触发
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    // 创建相机预览会话
    private void createCameraPreviewSession() {
        // 获取纹理视图的表面纹理
        SurfaceTexture texture = getSurfaceTexture();
        // 设置表面纹理的默认缓存尺寸
        texture.setDefaultBufferSize(mPreViewSize.getWidth(), mPreViewSize.getHeight());
        // 创建一个该表面纹理的表面对象
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 把纹理视图添加到预览目标
            mPreviewBuilder.addTarget(surface);
            // 设置自动对焦模式
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置自动曝光模式
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 开始对焦
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // 设置照片的方向
            mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, (mCameraType == CameraCharacteristics.LENS_FACING_FRONT) ? 90 : 270);

            // 创建一个相片捕获会话。此时预览画面显示在纹理视图上
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 相机准备就绪后，开启捕捉影像的会话
    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    // 影像配置就绪后，将预览画面呈现到手机屏幕上
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                Log.d(TAG, "onConfigured");
                mCameraSession = session;
                // 设置连拍请求。此时预览画面只会发给手机屏幕
                mCameraSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };

    // 一旦有图像数据生成，立刻触发onImageAvailable事件
    private OnImageAvailableListener onImageAvaiableListener = new OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.d(TAG, "onImageAvailable");
            mHandler.post(new ImageSaver(imageReader.acquireNextImage()));
            imageReader.acquireNextImage();

            //定义画笔
            Paint mpaint = new Paint();
            mpaint.setColor(Color.BLUE);
            // mpaint.setAntiAlias(true);//去锯齿
            mpaint.setStyle(Paint.Style.STROKE);//空心
            // 设置paint的外框宽度
            mpaint.setStrokeWidth(2f);

            Canvas canvas = new Canvas();

            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            Rect r = new Rect(0, 0, 100, 100);
            canvas.drawRect(r, mpaint);
            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    };

    // 定义一个图像保存任务
    private class ImageSaver implements Runnable {
        private Image mImage;

        public ImageSaver(Image reader) {
            mImage = reader;
        }

        @Override
        public void run() {
            // 获取本次拍摄的照片保存路径
            String path = String.format("%s%s.jpg", BitmapUtil.getCachePath(mContext), DateUtil.getNowDateTime());
            Log.d(TAG, "正在保存图片 path=" + path);
            // 保存图片文件
            BitmapUtil.saveBitmap(path, mImage.getPlanes()[0].getBuffer(), 4, "JPEG", 80);
            //BitmapUtil.setPictureDegreeZero(path);
            if (mImage != null) {
                mImage.close();
            }

            Log.d(TAG, "完成保存图片 path=" + path);
        }
    }

    private class CompareSizeByArea implements java.util.Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
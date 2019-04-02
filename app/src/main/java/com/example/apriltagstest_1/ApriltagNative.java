package com.example.apriltagstest_1;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Interface to native C AprilTag library.
 */

public class ApriltagNative {
    static {
        System.loadLibrary("native-lib");

        native_init();
    }

    public static native void native_init();


    public static native void apriltag_init(String tagFamily, int errorBits, double decimateFactor,
                                            double blurSigma, int nthreads);

    public static native ArrayList<ApriltagDetection> apriltag_detect_yuv(byte[] src, int width, int height);

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

}

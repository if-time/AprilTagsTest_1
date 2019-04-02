package com.example.apriltagstest_1;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TakeShootingActivity extends AppCompatActivity {
    private static final String      TAG       = "TakeShootingActivity";
    private              Camera2View camera2_view; // 声明一个二代相机视图对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_shooting);
        // 获取前一个页面传来的摄像头类型
        int camera_type = getIntent().getIntExtra("type", CameraCharacteristics.LENS_FACING_FRONT);
        // 从布局文件中获取名叫camera2_view的二代相机视图
        camera2_view = findViewById(R.id.camera2_view);
        // 设置二代相机视图的摄像头类型
        camera2_view.open(camera_type);
    }
}

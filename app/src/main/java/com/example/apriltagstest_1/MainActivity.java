package com.example.apriltagstest_1;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(MainActivity.this, "Andorid版本低于5.0无法使用camera2",
                    Toast.LENGTH_SHORT).show();
        } else if (PermissionUtil.checkPermission(this, Manifest.permission.CAMERA, 1)) {
            previewing();
        }
    }

    /**
     * 预览
     */
    private void previewing() {
        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String[] ids;
        try {
            // 获取摄像头的编号数组
            ids = cm.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
        if (checkCamera(ids, CameraCharacteristics.LENS_FACING_FRONT + "")) {
            // 前往camera2的拍照页面
            Intent intent = new Intent(this, TakeShootingActivity.class);
            // 类型为后置摄像头
            intent.putExtra("type", CameraCharacteristics.LENS_FACING_FRONT);
            // 需要处理拍照页面的返回结果
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(this, "当前设备不支持后置摄像头", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查是否存在指定类型的摄像头
     * @param ids
     * @param type
     * @return
     */
    private boolean checkCamera(String[] ids, String type) {
        boolean result = false;
        for (String id : ids) {
            if (id.equals(type)) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                previewing();
            } else {
                Toast.makeText(this, "需要允许相机权限才能查看相机信息噢", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.angcyo.camera.preview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.angcyo.camera.control.Camera1Control;
import com.angcyo.camera.control.ICameraControl;
import com.angcyo.camera.control.PermissionCallback;

/**
 * 只用来预览相机, 已经自动处理了 灭屏/亮屏的生命周期
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class CameraPreviewView extends FrameLayout {
    private static final String TAG = "CameraPreviewView";

    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    ICameraControl cameraControl;
    /**
     * 相机真正预览所在的View
     */
    View displayView;
    int visibilityOld = View.VISIBLE;
    /**
     * 当需要请求Camera权限的时候回调.
     */
    private PermissionCallback permissionCallback;

    public CameraPreviewView(Context context) {
        super(context);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cameraControl = new Camera2Control(getContext());
//        } else {
//            cameraControl = new Camera1Control(getContext());
//        }

        cameraControl = new Camera1Control(getContext());

        cameraControl.setPermissionCallback(getPermissionCallback());

        displayView = cameraControl.getDisplayView();
        addView(displayView);
    }

    private PermissionCallback getPermissionCallback() {
        if (permissionCallback == null) {
            //当没有自定义权限请求的时候, 使用默认的权限请求
            return new PermissionCallback() {
                @Override
                public boolean onRequestPermission() {
                    ActivityCompat.requestPermissions((Activity) getContext(),
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSIONS_REQUEST_CAMERA);
                    return false;
                }
            };
        }
        return permissionCallback;
    }

    /**
     * 设置需要请求权限时的回调,
     * 权限请求通过后, 请调用 {@link #onRequestPermissionsResult(int, int[])}
     */
    public void setPermissionCallback(PermissionCallback permissionCallback) {
        this.permissionCallback = permissionCallback;
    }

    public int getFlashMode() {
        return cameraControl.getFlashMode();
    }

    public boolean isFlashOff() {
        return getFlashMode() == ICameraControl.FLASH_MODE_OFF;
    }

    /**
     * 打开or关闭 闪关灯
     */
    public void toggleFlashMode() {
        if (cameraControl.getFlashMode() == ICameraControl.FLASH_MODE_OFF) {
            cameraControl.setFlashMode(ICameraControl.FLASH_MODE_TORCH);
        } else {
            cameraControl.setFlashMode(ICameraControl.FLASH_MODE_OFF);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraControl.refreshPermission();
            } else {
                Log.e(TAG, "Camera 权限请求失败");
            }
        }
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        Log.i(TAG, "...onAttachedToWindow");
//        //cameraControl.start();
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        Log.i(TAG, "...onDetachedFromWindow");
//        cameraControl.stop();
//    }

//    @Override
//    protected void onWindowVisibilityChanged(int visibility) {
//        super.onWindowVisibilityChanged(visibility);
//    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        //Log.i(TAG, "...onVisibilityChanged:" + visibility);

        if (visibility == View.VISIBLE) {
            if (visibilityOld != View.VISIBLE) {
                cameraControl.resume();
            }
        } else {
            cameraControl.pause();
        }

        visibilityOld = visibility;
    }
}

package com.angcyo.rcode;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.angcyo.rcode.camera.CameraManager;


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/02/27 14:00
 * 修改人员：Robi
 * 修改时间：2018/02/27 14:00
 * 修改备注：
 * Version: 1.0.0
 */
public interface IActivity {
    CameraManager getCameraManager();

    Handler getHandler();

    Activity getHandleActivity();

    ViewfinderView getViewfinderView();

    /**
     * 扫描返回后的结果
     */
    void handleDecode(String data);

    void setResult(int resultCode, Intent data);

    void finish();

    PackageManager getPackageManager();

    void startActivity(Intent intent);

    void drawViewfinder();

}

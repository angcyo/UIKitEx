package com.angcyo.camera;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class RecordVideoCallback {
    /**
     * 允许最大录制时长 (秒)
     */
    public int maxRecordTime = 10;

    /**
     * 最小录制时长 (秒)
     */
    public int minRecordTime = 3;

    /**
     * 拍照回调
     */
    public void onTakePhoto(@NonNull Bitmap bitmap, @NonNull File outputFile) {

    }

    /**
     * 录像回调
     */
    public void onTakeVideo(@NonNull String videoPath) {

    }
}

package com.angcyo.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.angcyo.camera.preview.TakePicturePreview;
import com.angcyo.uiview.less.base.BaseFragment;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

import java.io.File;

/**
 * 简单的拍照Fragment, 带闪光灯, 带拍照确认界面
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class TakePictureFragment extends BaseFragment {
    TakePicturePreview.OnTakePictureCallback onTakePictureCallback;
    boolean needConfirm = true;
    File outputFile;
    TakePicturePreview takePicturePreview;

    public static boolean saveBitmap(File outputFile, Bitmap bitmap) {
        return TakePicturePreview.saveBitmap(outputFile, bitmap);
    }

    public static TakePictureFragment show(FragmentManager fragmentManager,
                                           boolean needConfirm,
                                           File outputFile,
                                           TakePicturePreview.OnTakePictureCallback takePictureCallback
    ) {
        TakePictureFragment fragment = new TakePictureFragment();
        fragment.onTakePictureCallback = takePictureCallback;
        fragment.needConfirm = needConfirm;
        fragment.outputFile = outputFile;
        FragmentHelper.build(fragmentManager)
                .defaultEnterAnim()
                .showFragment(fragment)
                .doIt();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_fragment_take_picture;
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);

        takePicturePreview = viewHolder.v(R.id.camera_take_picture_view);
        takePicturePreview.setNeedConfirm(needConfirm);
        takePicturePreview.setOutputFile(outputFile);
        takePicturePreview.setOnTakePictureCallback(new TakePicturePreview.OnTakePictureCallback() {
            @Override
            public void onPictureTaken(@NonNull Bitmap bitmap, @Nullable File outputFile) {
                if (onTakePictureCallback != null) {
                    onTakePictureCallback.onPictureTaken(bitmap, outputFile);
                }

                backFragment(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (takePicturePreview != null) {
            takePicturePreview.onRequestPermissionsResult(requestCode, grantResults);
        }
    }
}

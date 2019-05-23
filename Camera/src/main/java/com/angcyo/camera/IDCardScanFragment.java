package com.angcyo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import com.angcyo.camera.preview.IDCardPreview;
import com.angcyo.camera.preview.TakePicturePreview;
import com.angcyo.tesstwo.Tesstwo;
import com.angcyo.uiview.less.base.BaseFragment;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;

import java.io.File;

/**
 * 身份证号码识别
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class IDCardScanFragment extends BaseFragment {

    IDCardPreview idCardPreview;
    Tesstwo tesstwo;

    Tesstwo.OnResultCallback onResultCallback;

    public static boolean saveBitmap(File outputFile, Bitmap bitmap) {
        return TakePicturePreview.saveBitmap(outputFile, bitmap);
    }

    public static IDCardScanFragment show(FragmentManager fragmentManager,
                                          Tesstwo.OnResultCallback onResultCallback
    ) {
        IDCardScanFragment fragment = new IDCardScanFragment();
        fragment.onResultCallback = onResultCallback;
        FragmentHelper.build(fragmentManager)
                .defaultEnterAnim()
                .showFragment(fragment)
                .doIt();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Tesstwo.init(context.getApplicationContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tesstwo = new Tesstwo();

        tesstwo.setOnResultCallback(new Tesstwo.OnResultCallback() {
            @Override
            public void onResult(@NonNull final Bitmap cardBitmap, @NonNull final Bitmap cardNoBitmap, final String idCardNo) {
                backFragment(false);

                if (onResultCallback != null) {
                    baseViewHolder.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onResultCallback != null) {
                                onResultCallback.onResult(cardBitmap, cardNoBitmap, idCardNo);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tesstwo.setOnResultCallback(null);
        tesstwo.release();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_fragment_id_card_scan;
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);

        idCardPreview = viewHolder.v(R.id.camera_id_card_view);
        idCardPreview.setOnIDCardPictureCallback(new IDCardPreview.OnIDCardPictureCallback() {
            @Override
            public boolean onIDCardPicture(@NonNull Bitmap idCardBitmap) {
                return scanBitmap(idCardBitmap);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (idCardPreview != null) {
            idCardPreview.onRequestPermissionsResult(requestCode, grantResults);
        }
    }

    /**
     * @return 返回true, 表示停止预览, 返回false 继续预览
     */
    protected boolean scanBitmap(@NonNull Bitmap idCardBitmap) {
        tesstwo.localre(idCardBitmap);
        return isFragmentHide();
    }
}

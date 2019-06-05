package com.angcyo.opencv;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.base.BaseFragment;
import com.angcyo.uiview.less.base.helper.FragmentHelper;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import exocr.exocrengine.EXOCRModel;
import exocr.exocrengine.OcrSurfaceView;
import org.jetbrains.annotations.Nullable;

/**
 * 身份证号码识别
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class CardOcrScanFragment extends BaseFragment {

    OcrSurfaceView.OnOcrChangeListener onResultCallback;
    int oldOrientation = -999;

    public static CardOcrScanFragment show(FragmentManager fragmentManager,
                                           OcrSurfaceView.OnOcrChangeListener onResultCallback
    ) {
        CardOcrScanFragment fragment = new CardOcrScanFragment();
        fragment.onResultCallback = onResultCallback;
        FragmentHelper.build(fragmentManager)
                .showFragment(fragment)
                .doIt();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CardOcr.init(context.getApplicationContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (oldOrientation != -999) {
            getActivity().setRequestedOrientation(oldOrientation);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.card_ocr_scan_layout;
    }

    @Override
    protected void initBaseView(@NonNull RBaseViewHolder viewHolder, @Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
        super.initBaseView(viewHolder, arguments, savedInstanceState);

        oldOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        OcrSurfaceView surfaceView = viewHolder.v(R.id.surface);
        surfaceView.setOnOcrChangeListener(new OcrSurfaceView.OnOcrChangeListener() {
            @Override
            public void onSuccess(final EXOCRModel exocrModel) {
                L.i("识别结果:" + exocrModel);
                vibrate();

                backFragment(false);

                if (onResultCallback != null) {
                    baseViewHolder.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onResultCallback != null) {
                                onResultCallback.onSuccess(exocrModel);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 震动提醒
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) mAttachContext.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200L);
    }
}
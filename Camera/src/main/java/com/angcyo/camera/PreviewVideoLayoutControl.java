package com.angcyo.camera;

import android.view.View;
import com.angcyo.camera.play.TextureVideoView;
import com.angcyo.uiview.less.kotlin.ViewExKt;
import com.angcyo.uiview.less.utils.RUtils;

import java.io.File;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class PreviewVideoLayoutControl {

    View parent;

    public PreviewVideoLayoutControl(View parent) {
        this.parent = parent;
    }

    public void showPreview(final String videoPath, final Runnable onCancel) {
        parent.setVisibility(View.VISIBLE);
        View cancelButton = parent.findViewById(R.id.video_cancel_button);
        View confirmButton = parent.findViewById(R.id.video_confirm_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.setVisibility(View.GONE);
                onCancel.run();
            }
        });

        cancelButton.setTranslationX(ViewExKt.getDpi(cancelButton) * 100);
        cancelButton.animate().translationX(0).setDuration(300).start();

        confirmButton.setTranslationX(-ViewExKt.getDpi(cancelButton) * 100);
        confirmButton.animate().translationX(0).setDuration(300).start();

        final TextureVideoView videoView = parent.findViewById(R.id.video_view);
        videoView.setRepeatPlay(true);
        videoView.setVideoURI(RUtils.getFileUri(RUtils.getApp(), new File(videoPath)));
        videoView.start();
    }
}

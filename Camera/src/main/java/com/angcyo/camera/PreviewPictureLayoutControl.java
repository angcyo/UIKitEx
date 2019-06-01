package com.angcyo.camera;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import com.angcyo.uiview.less.kotlin.ViewExKt;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/06/01
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class PreviewPictureLayoutControl {

    View parent;

    public PreviewPictureLayoutControl(View parent) {
        this.parent = parent;
    }

    public void showPreview(Bitmap bitmap) {
        parent.setVisibility(View.VISIBLE);
        ((ImageView) parent.findViewById(R.id.camera_display_image_view)).setImageBitmap(bitmap);

        View cancelButton = parent.findViewById(R.id.camera_cancel_button);
        View confirmButton = parent.findViewById(R.id.camera_confirm_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.setVisibility(View.GONE);
            }
        });

        cancelButton.setTranslationX(ViewExKt.getDpi(cancelButton) * 100);
        cancelButton.animate().translationX(0).setDuration(300).start();

        confirmButton.setTranslationX(-ViewExKt.getDpi(cancelButton) * 100);
        confirmButton.animate().translationX(0).setDuration(300).start();
    }
}

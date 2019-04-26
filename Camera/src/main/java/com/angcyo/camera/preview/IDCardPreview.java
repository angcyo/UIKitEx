package com.angcyo.camera.preview;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import com.angcyo.camera.R;
import com.angcyo.camera.control.Camera1Control;
import com.angcyo.camera.control.ICameraControl;
import com.angcyo.camera.util.DimensionUtil;
import com.angcyo.camera.util.ImageUtil;

import java.io.IOException;

/**
 * 包含了取景框, 闪光灯功能
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/04/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class IDCardPreview extends CameraPreviewView {
    public static int ORIENTATION_PORTRAIT = 0;
    public static int ORIENTATION_HORIZONTAL = 1;
    /**
     * 身份证，银行卡，等裁剪用的遮罩
     */
    MaskView maskView;
    /**
     * 用于显示提示证 "请对齐身份证正面" 之类的背景
     */
    ImageView hintView;

    /**
     * 闪光灯按钮
     */
    ImageView lightButton;

    OnIDCardPictureCallback onIDCardPictureCallback;

    public IDCardPreview(Context context) {
        super(context);
    }

    public IDCardPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();

        setOrientation(getResources().getConfiguration());

        //黑色背景, 白色矩形框的遮罩
        maskView = new MaskView(getContext());
        addView(maskView);

        //提示图片
        hintView = new ImageView(getContext());
        addView(hintView);
        hintView.setImageResource(R.drawable.bd_ocr_hint_align_id_card);

        //闪光灯
        lightButton = new ImageView(getContext());
        LayoutParams params = new LayoutParams(-2, -2);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        addView(lightButton, params);
        updateFlashMode();
        lightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlashMode();
                updateFlashMode();
            }
        });
    }

    private void updateFlashMode() {
        int flashMode = cameraControl.getFlashMode();
        if (flashMode == ICameraControl.FLASH_MODE_TORCH) {
            lightButton.setImageResource(R.drawable.bd_ocr_light_on);
        } else {
            lightButton.setImageResource(R.drawable.bd_ocr_light_off);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setKeepScreenOn(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setKeepScreenOn(false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int maskBottom = 0;
        if (maskView != null) {
            maskView.layout(left, 0, right, bottom - top);

            maskBottom = maskView.getFrameRectExtend().bottom;
        }

        int hintViewWidth = DimensionUtil.dpToPx(250);
        int hintViewHeight = DimensionUtil.dpToPx(25);
        int hintViewLeft = (getWidth() - hintViewWidth) / 2;
        int hintViewTop = maskBottom + DimensionUtil.dpToPx(16);

        if (hintView != null) {
            hintView.layout(hintViewLeft, hintViewTop,
                    hintViewLeft + hintViewWidth, hintViewTop + hintViewHeight);
        }

        if (lightButton != null) {
            int lightTop = hintViewTop + hintViewHeight + hintViewHeight;
            lightButton.layout(lightButton.getLeft(), lightTop,
                    lightButton.getRight(), lightTop + lightButton.getMeasuredHeight());
        }
    }

    public void setOnIDCardPictureCallback(OnIDCardPictureCallback onIDCardPictureCallback) {
        this.onIDCardPictureCallback = onIDCardPictureCallback;

        if (onIDCardPictureCallback != null) {
            //设置这个回调之后, 才会有帧数据回调
            cameraControl.setDetectCallback(new ICameraControl.OnDetectPictureCallback() {
                @Override
                public int onDetect(byte[] data, int rotation) {
                    if (IDCardPreview.this.onIDCardPictureCallback == null) {
                        return Camera1Control.MODEL_SCAN;
                    }
                    int detect = detect(data, rotation);
                    return detect;
                }
            });
        }
    }

    public void setOrientation(@CameraView.Orientation int orientation) {
        cameraControl.setDisplayOrientation(orientation);
    }

    private void setOrientation(Configuration newConfig) {
        int rotation = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int orientation;
        int cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                cameraViewOrientation = CameraView.ORIENTATION_PORTRAIT;
                orientation = ORIENTATION_PORTRAIT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = ORIENTATION_HORIZONTAL;
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    cameraViewOrientation = CameraView.ORIENTATION_HORIZONTAL;
                } else {
                    cameraViewOrientation = CameraView.ORIENTATION_INVERT;
                }
                break;
            default:
                orientation = ORIENTATION_PORTRAIT;
                setOrientation(CameraView.ORIENTATION_PORTRAIT);
                break;
        }
        setOrientation(cameraViewOrientation);
    }

    /**
     * 拿到取景框内的图片
     */
    private int detect(byte[] data, final int rotation) {
        // 扫描成功阻止多余的操作
        if (cameraControl.getAbortingScan().get()) {
            return 0;
        }
        // BitmapRegionDecoder不会将整个图片加载到内存。
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (decoder == null) {
            return Camera1Control.MODEL_SCAN;
        }

        Rect previewFrame = cameraControl.getPreviewFrame();

        int width = rotation % 180 == 0 ? decoder.getWidth() : decoder.getHeight();
        int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();

        Rect frameRect = maskView.getFrameRectExtend();

        int left = width * frameRect.left / maskView.getWidth();
        int top = height * frameRect.top / maskView.getHeight();
        int right = width * frameRect.right / maskView.getWidth();
        int bottom = height * frameRect.bottom / maskView.getHeight();

        // 高度大于图片
        if (previewFrame.top < 0) {
            // 宽度对齐。
            int adjustedPreviewHeight = previewFrame.height() * getWidth() / previewFrame.width();
            int topInFrame = ((adjustedPreviewHeight - frameRect.height()) / 2)
                    * getWidth() / previewFrame.width();
            int bottomInFrame = ((adjustedPreviewHeight + frameRect.height()) / 2) * getWidth()
                    / previewFrame.width();

            // 等比例投射到照片当中。
            top = topInFrame * height / previewFrame.height();
            bottom = bottomInFrame * height / previewFrame.height();
        } else {
            // 宽度大于图片
            if (previewFrame.left < 0) {
                // 高度对齐
                int adjustedPreviewWidth = previewFrame.width() * getHeight() / previewFrame.height();
                int leftInFrame = ((adjustedPreviewWidth - maskView.getFrameRect().width()) / 2) * getHeight()
                        / previewFrame.height();
                int rightInFrame = ((adjustedPreviewWidth + maskView.getFrameRect().width()) / 2) * getHeight()
                        / previewFrame.height();

                // 等比例投射到照片当中。
                left = leftInFrame * width / previewFrame.width();
                right = rightInFrame * width / previewFrame.width();
            }
        }

        Rect region = new Rect();
        region.left = left;
        region.top = top;
        region.right = right;
        region.bottom = bottom;

        // 90度或者270度旋转
        if (rotation % 180 == 90) {
            int x = decoder.getWidth() / 2;
            int y = decoder.getHeight() / 2;

            int rotatedWidth = region.height();
            int rotated = region.width();

            // 计算，裁剪框旋转后的坐标
            region.left = x - rotatedWidth / 2;
            region.top = y - rotated / 2;
            region.right = x + rotatedWidth / 2;
            region.bottom = y + rotated / 2;
            region.sort();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        // 最大图片大小。
        int maxPreviewImageSize = 2560;
        int size = Math.min(decoder.getWidth(), decoder.getHeight());
        size = Math.min(size, maxPreviewImageSize);

        options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
        options.inScaled = true;
        options.inDensity = Math.max(options.outWidth, options.outHeight);
        options.inTargetDensity = size;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = decoder.decodeRegion(region, options);
        if (!decoder.isRecycled()) {
            decoder.recycle();
        }

        if (rotation != 0) {
            // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                // 有时候 createBitmap会复用对象
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        int status = Camera1Control.MODEL_SCAN;
        if (onIDCardPictureCallback != null) {
            if (onIDCardPictureCallback.onIDCardPicture(bitmap)) {
                status = Camera1Control.MODEL_NOSCAN;
            }
        }

        return status;
    }

    public interface OnIDCardPictureCallback {
        /**
         * @param idCardBitmap 取景框区域内的图片
         * @return 返回true, 表示停止预览, 返回false 继续预览
         */
        boolean onIDCardPicture(@NonNull Bitmap idCardBitmap);
    }
}
